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
import io.netty.buffer.Unpooled;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.NettyPipeline;
import reactor.netty.udp.UdpClient;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class VoiceSocket {

    static final String PROTOCOL = "udp";
    static final String ENCRYPTION_MODE = "xsalsa20_poly1305";
    private static final int DISCOVERY_PACKET_LENGTH = 70;

    private final EmitterProcessor<ByteBuf> inbound = EmitterProcessor.create(false);
    private final EmitterProcessor<ByteBuf> outbound = EmitterProcessor.create(false);

    private final FluxSink<ByteBuf> inboundSink = inbound.sink(FluxSink.OverflowStrategy.LATEST);

    Mono<Void> setup(String address, int port) {
        return UdpClient.create()
                .wiretap(true)
                .host(address)
                .port(port)
                .handle((in, out) -> {
                    Mono<Void> inboundThen = in.receive()
                            .log("discord4j.voice.udp.inbound", Level.FINEST)
                            .doOnNext(this.inboundSink::next)
                            .then();

                    Mono<Void> outboundThen = out.options(NettyPipeline.SendOptions::flushOnEach)
                            .send(outbound.log("discord4j.voice.udp.outbound", Level.FINEST))
                            .then();

                    return Mono.zip(inboundThen, outboundThen).then();
                })
                .connect()
                .then();
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
                    String address = getNullTerminatedString(buf, Integer.BYTES); // undocumented: discord replies with the ssrc first, THEN the IP address
                    int port = buf.getUnsignedShortLE(DISCOVERY_PACKET_LENGTH - Short.BYTES);

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
