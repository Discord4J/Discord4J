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
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyPipeline;
import reactor.netty.udp.UdpClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class VoiceSocket {

    static final String ENCRYPTION_MODE = "xsalsa20_poly1305";
    private static final int DISCOVERY_PACKET_LENGTH = 70;

    private final EmitterProcessor<ByteBuf> inbound = EmitterProcessor.create(false);
    private final EmitterProcessor<ByteBuf> outbound = EmitterProcessor.create(false);

    private byte[] secretKey;

    Mono<? extends Connection> setup(String address, int port) {
        System.out.println("setup");
        return UdpClient.create()
                .wiretap()
                .host(address)
                .port(port)
                .handle((in, out) -> {
                    Mono<Void> inboundThen = in.receive().log("udp inbound").doOnNext(this.inbound::onNext).then();
                    Mono<Void> outboundThen = out.options(NettyPipeline.SendOptions::flushOnEach).send(outbound.log("udp outbound")).then();

                    return Mono.zip(inboundThen, outboundThen).then();
                })
                .connect();
    }

    Mono<Tuple2<String, Integer>> performIpDiscovery(int ssrc) {
        Mono<Void> sendDiscoveryPacket = Mono.fromRunnable(() -> {
            ByteBuffer buf = ByteBuffer.allocate(DISCOVERY_PACKET_LENGTH).putInt(ssrc);
            buf.position(0);
            ByteBuf discoveryPacket = Unpooled.wrappedBuffer(buf);

            System.out.println("got here");
            outbound.onNext(discoveryPacket);
        });

        Mono<Tuple2<String, Integer>> parseResponse = inbound.next()
                .map(buf -> {
                    buf.skipBytes(4);
                    ByteArrayOutputStream os = new ByteArrayOutputStream(32);
                    while (true) {
                        byte c = buf.readByte();
                        if (c == '\0') break;
                        os.write(c);
                    }
                    String address = new String(os.toByteArray());
                    int port = buf.getUnsignedShortLE(DISCOVERY_PACKET_LENGTH - Short.BYTES);

                    return Tuples.of(address, port);
                });

        return sendDiscoveryPacket.then(parseResponse);
    }

    ByteBufFlux inbound() {
        return ByteBufFlux.fromInbound(inbound);
    }

    void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }
}