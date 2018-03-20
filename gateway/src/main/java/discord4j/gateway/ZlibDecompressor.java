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
package discord4j.gateway;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Implements a zlib inflater on a stream of {@link io.netty.buffer.ByteBuf} elements.
 */
public class ZlibDecompressor {

    private static final int ZLIB_SUFFIX = 0x0000FFFF;
    private static final Predicate<ByteBuf> windowPredicate = payload ->
            payload.readableBytes() >= 4 && payload.getInt(payload.readableBytes() - 4) == ZLIB_SUFFIX;

    private final Inflater context = new Inflater();

    public Flux<ByteBuf> completeMessages(Flux<ByteBuf> payloads) {
        return payloads.windowUntil(windowPredicate)
                .flatMap(Flux::collectList)
                .map(list -> {
                    ByteBuf buf = Unpooled.wrappedBuffer(list.toArray(new ByteBuf[0]));
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (InflaterOutputStream inflater = new InflaterOutputStream(out, context)) {
                        inflater.write(bytes);
                        return Unpooled.wrappedBuffer(out.toByteArray());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }
}
