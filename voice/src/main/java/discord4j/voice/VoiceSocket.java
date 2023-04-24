/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.voice;

import discord4j.common.sinks.EmissionStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.udp.UdpClient;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;
import reactor.util.context.ContextView;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static discord4j.common.LogUtil.format;

/**
 * A UDP client abstraction dedicated to handling the transport of raw voice packets.
 */
public class VoiceSocket {

    private static final Logger log = Loggers.getLogger(VoiceSocket.class);
    private static final Logger senderLog = Loggers.getLogger("discord4j.voice.protocol.udp.sender");
    private static final Logger receiverLog = Loggers.getLogger("discord4j.voice.protocol.udp.receiver");

    static final String PROTOCOL = "udp";
    static final String ENCRYPTION_MODE = "xsalsa20_poly1305";
    private static final int DISCOVERY_PACKET_LENGTH = 74;
    private static final int TYPE_LENGTH_SSRC_LENGTH = Short.BYTES + Short.BYTES + Integer.BYTES;

    private final UdpClient udpClient;
    private final Sinks.Many<ByteBuf> inbound;
    private final Sinks.Many<ByteBuf> outbound;
    private final EmissionStrategy emissionStrategy;

    public VoiceSocket(UdpClient udpClient) {
        this.udpClient = udpClient;
        this.inbound = newEmitterSink();
        this.outbound = newEmitterSink();
        this.emissionStrategy = EmissionStrategy.timeoutDrop(Duration.ofSeconds(5));
    }

    private static <T> Sinks.Many<T> newEmitterSink() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    Mono<Connection> setup(String address, int port) {
        return Mono.deferContextual(
                context -> udpClient.host(address).port(port)
                        .observe(getObserver(context))
                        .doOnConnected(c -> log.debug(format(context, "Connected to {}"), address(c)))
                        .doOnDisconnected(c -> log.debug(format(context, "Disconnected from {}"), address(c)))
                        .handle((in, out) -> {
                            Mono<Void> inboundThen = in.receive().retain()
                                    .doOnNext(buf -> logPayload(receiverLog, context, buf))
                                    .doOnNext(buf -> emissionStrategy.emitNext(inbound, buf))
                                    .then();

                            Mono<Void> outboundThen = out.send(outbound.asFlux()
                                    .doOnNext(buf -> logPayload(senderLog, context, buf)))
                                    .then();

                            in.withConnection(c -> c.onDispose(() -> log.debug(format(context, "Connection disposed"))));

                            return Mono.zip(inboundThen, outboundThen).then();
                        })
                        .connect());
    }

    private SocketAddress address(Connection connection) {
        Channel c = connection.channel();
        if (c instanceof DatagramChannel) {
            SocketAddress a = c.remoteAddress();
            return a != null ? a : c.localAddress();
        }
        return c.remoteAddress();
    }

    private ConnectionObserver getObserver(ContextView context) {
        return (connection, newState) -> log.debug(format(context, "{} {}"), newState, connection);
    }

    private void logPayload(Logger logger, ContextView context, ByteBuf buf) {
        logger.trace(format(context, ByteBufUtil.hexDump(buf)));
    }

    Mono<InetSocketAddress> performIpDiscovery(int ssrc) {
        // https://discord.com/developers/docs/topics/voice-connections#ip-discovery
        Mono<Void> sendDiscoveryPacket = Mono.fromRunnable(() -> {
            // Build request packet
            // Type: Values 0x1 and 0x2 indicate request and response, respectively (2 bytes)
            // Length: Message length excluding Type and Length fields (value 70) (2 bytes)
            // SSRC: Unsigned integer (4 bytes)
            // Remaining bytes are zero
            ByteBuf discoveryPacket = Unpooled.buffer(DISCOVERY_PACKET_LENGTH)
                    .writeShort(1)
                    .writeShort(70)
                    .writeInt(ssrc)
                    .writeZero(DISCOVERY_PACKET_LENGTH - TYPE_LENGTH_SSRC_LENGTH);

            emissionStrategy.emitNext(outbound, discoveryPacket);
        });

        Mono<InetSocketAddress> parseResponse = inbound.asFlux()
                .next()
                .map(buf -> {
                    // Decode response packet
                    // Skip Type, Length and SSRC fields (TYPE_LENGTH_SSRC_LENGTH)
                    // Address: Null-terminated string in response (64 bytes)
                    // Port: Unsigned short (2 bytes)
                    String address = getNullTerminatedString(buf, TYPE_LENGTH_SSRC_LENGTH);
                    int port = buf.getUnsignedShortLE(DISCOVERY_PACKET_LENGTH - Short.BYTES);
                    buf.release();
                    return InetSocketAddress.createUnresolved(address, port);
                });

        return sendDiscoveryPacket.then(parseResponse);
    }

    void send(ByteBuf data) {
        emissionStrategy.emitNext(outbound, data);
    }

    Flux<ByteBuf> getInbound() {
        return inbound.asFlux();
    }

    private static String getNullTerminatedString(ByteBuf buffer, int offset) {
        buffer.skipBytes(offset);
        ByteArrayOutputStream os = new ByteArrayOutputStream(15);
        byte c;
        while ((c = buffer.readByte()) != 0) {
            os.write(c);
        }

        return new String(os.toByteArray(), StandardCharsets.US_ASCII);
    }
}
