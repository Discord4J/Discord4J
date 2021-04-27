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

import io.netty.buffer.*;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Implements a zlib inflater on a stream of {@link ByteBuf} elements.
 */
public class ZlibDecompressor {

    private static final int ZLIB_SUFFIX = 0x0000FFFF;
    private static final Predicate<ByteBuf> windowPredicate = payload ->
            payload.readableBytes() >= 4 && payload.getInt(payload.readableBytes() - 4) == ZLIB_SUFFIX;

    private final ByteBufAllocator allocator;
    private final Inflater context = new Inflater();
    private final boolean unpooled;

    public ZlibDecompressor(ByteBufAllocator allocator) {
        this(allocator, false);
    }

    public ZlibDecompressor(ByteBufAllocator allocator, boolean unpooled) {
        this.allocator = allocator;
        this.unpooled = unpooled;
    }

    public Flux<ByteBuf> completeMessages(Flux<ByteBuf> payloads) {
        return payloads.windowUntil(windowPredicate)
                .flatMap(Flux::collectList)
                .map(list -> {
                    final ByteBuf buf;
                    if (list.size() == 1) {
                        buf = list.get(0);
                    } else {
                        CompositeByteBuf composite = allocator.compositeBuffer(list.size());
                        for (ByteBuf component : list) {
                            composite.addComponent(true, component);
                        }
                        buf = composite;
                    }
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (InflaterOutputStream inflater = new InflaterOutputStream(out, context)) {
                        inflater.write(ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false));
                        ByteBuf outBuffer = unpooled ? Unpooled.buffer() : allocator.buffer();
                        return outBuffer.writeBytes(out.toByteArray()).asReadOnly();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }
}
