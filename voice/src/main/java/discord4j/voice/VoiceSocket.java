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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.udp.UdpClient;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static discord4j.common.LogUtil.format;

public class VoiceSocket {

    private static final Logger log = Loggers.getLogger(VoiceSocket.class);

    static final String PROTOCOL = "udp";
    static final String ENCRYPTION_MODE = "xsalsa20_poly1305";
    private static final int DISCOVERY_PACKET_LENGTH = 70;

    private final EmitterProcessor<ByteBuf> inbound = EmitterProcessor.create(false);
    private final EmitterProcessor<ByteBuf> outbound = EmitterProcessor.create(false);

    private final FluxSink<ByteBuf> inboundSink = inbound.sink(FluxSink.OverflowStrategy.LATEST);
    private final UdpClient udpClient;

    public VoiceSocket(UdpClient udpClient) {
        this.udpClient = udpClient;
    }

    Mono<Void> setup(String address, int port) {
        return Mono.subscriberContext()
                .flatMap(context -> udpClient.host(address).port(port)
                        .doOnConnected(c -> log.info("Connected to {}", c.address()))
                        .doOnDisconnected(c -> log.info("Disconnected from {}", c.address()))
                        .handle((in, out) -> {
                            Mono<Void> inboundThen = in.receive().retain()
                                    .doOnNext(buf -> logPayload("<< ", context, buf))
                                    .doOnNext(this.inboundSink::next)
                                    .then();

                            Mono<Void> outboundThen = out.send(outbound
                                    .doOnNext(buf -> logPayload(">> ", context, buf)))
                                    .then();

                            return Mono.zip(inboundThen, outboundThen).then();
                        })
                        .connect()
                        .then());
    }

    private void logPayload(String prefix, Context context, ByteBuf buf) {
        log.trace(format(context, prefix + ByteBufUtil.hexDump(buf)));
    }

    Mono<InetSocketAddress> performIpDiscovery(int ssrc) {
        Mono<Void> sendDiscoveryPacket = Mono.fromRunnable(() -> {
            ByteBuf discoveryPacket = Unpooled.buffer(DISCOVERY_PACKET_LENGTH)
                    .writeInt(ssrc)
                    .writeZero(DISCOVERY_PACKET_LENGTH - Integer.BYTES);

            outbound.onNext(discoveryPacket);
        });

        Mono<InetSocketAddress> parseResponse = inbound.next()
                .map(buf -> {
                    // undocumented: discord replies with the ssrc first, THEN the IP address
                    String address = getNullTerminatedString(buf, Integer.BYTES);
                    int port = buf.getUnsignedShortLE(DISCOVERY_PACKET_LENGTH - Short.BYTES);
                    buf.release();
                    return InetSocketAddress.createUnresolved(address, port);
                });

        return sendDiscoveryPacket.then(parseResponse);
    }

    void send(ByteBuf data) {
        outbound.onNext(data);
    }

    Flux<ByteBuf> getInbound() {
        return inbound;
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
