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
import reactor.core.publisher.UnicastProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class ZlibDecompressor {

	private static final int ZLIB_SUFFIX = 0x0000FFFF;

	private final UnicastProcessor<ByteBuf> completeMessages = UnicastProcessor.create();
	private final Inflater context = new Inflater();
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public void push(ByteBuf buf) throws IOException {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		buffer.write(bytes);

		if (bytes.length < 4 || buf.getInt(bytes.length - 4) != ZLIB_SUFFIX) {
			return;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length * 2);
		try (InflaterOutputStream inflater = new InflaterOutputStream(out, context)) {
			inflater.write(buffer.toByteArray());
			completeMessages.onNext(Unpooled.wrappedBuffer(out.toByteArray()));
		} finally {
			buffer.reset();
		}
	}

	public UnicastProcessor<ByteBuf> completeMessages() {
		return completeMessages;
	}
}
