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
import io.netty.buffer.UnpooledDirectByteBuf;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class VoiceSocket {

    static final String PROTOCOL = "udp";
    static final String ENCRYPTION_MODE = "xsalsa20_poly1305";
    private static final int DISCOVERY_PACKET_LENGTH = 70;

    private final EmitterProcessor<ByteBuf> inbound = EmitterProcessor.create(false);
    private final EmitterProcessor<ByteBuf> outbound = EmitterProcessor.create(false);

    Mono<Void> setup(String address, int port) {
        return UdpClient.create()
                .wiretap()
                .host(address)
                .port(port)
                .handle((in, out) -> {
                    Mono<Void> inboundThen = in.receive()
                            .log("udp inbound")
                            .doOnNext(this.inbound::onNext)
                            .then();

                    Mono<Void> outboundThen = out.options(NettyPipeline.SendOptions::flushOnEach)
                            .send(outbound.log("udp outbound"))
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