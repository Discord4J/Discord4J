/**
 * Copyright 2002-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package discord4j.gateway.buffer;

import io.netty.buffer.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * Implementation of the {@code DataBuffer} interface that wraps a Netty {@link io.netty.buffer.ByteBuf}. Typically
 * constructed with {@link NettyDataBufferFactory}.
 *
 * @author Arjen Poutsma
 */
public class NettyDataBuffer {

	private final NettyDataBufferFactory dataBufferFactory;

	private ByteBuf byteBuf;


	/**
	 * Creates a new {@code NettyDataBuffer} based on the given {@code ByteBuff}.
	 *
	 * @param byteBuf the buffer to base this buffer on
	 */
	NettyDataBuffer(ByteBuf byteBuf, NettyDataBufferFactory dataBufferFactory) {
		this.byteBuf = byteBuf;
		this.dataBufferFactory = dataBufferFactory;
	}


	/**
	 * Directly exposes the native {@code ByteBuf} that this buffer is based on.
	 *
	 * @return the wrapped byte buffer
	 */
	public ByteBuf getNativeBuffer() {
		return this.byteBuf;
	}

	public NettyDataBufferFactory factory() {
		return this.dataBufferFactory;
	}

	public int indexOf(IntPredicate predicate, int fromIndex) {
		if (fromIndex < 0) {
			fromIndex = 0;
		} else if (fromIndex >= this.byteBuf.writerIndex()) {
			return -1;
		}
		int length = this.byteBuf.writerIndex() - fromIndex;
		return this.byteBuf.forEachByte(fromIndex, length, predicate.negate()::test);
	}

	public int lastIndexOf(IntPredicate predicate, int fromIndex) {
		if (fromIndex < 0) {
			return -1;
		}
		fromIndex = Math.min(fromIndex, this.byteBuf.writerIndex() - 1);
		return this.byteBuf.forEachByteDesc(0, fromIndex + 1, predicate.negate()::test);
	}

	public int readableByteCount() {
		return this.byteBuf.readableBytes();
	}

	public byte read() {
		return this.byteBuf.readByte();
	}

	public NettyDataBuffer read(byte[] destination) {
		this.byteBuf.readBytes(destination);
		return this;
	}

	public NettyDataBuffer read(byte[] destination, int offset, int length) {
		this.byteBuf.readBytes(destination, offset, length);
		return this;
	}

	public NettyDataBuffer write(byte b) {
		this.byteBuf.writeByte(b);
		return this;
	}

	public NettyDataBuffer write(byte[] source) {
		this.byteBuf.writeBytes(source);
		return this;
	}

	public NettyDataBuffer write(byte[] source, int offset, int length) {
		this.byteBuf.writeBytes(source, offset, length);
		return this;
	}

	public NettyDataBuffer write(NettyDataBuffer... buffers) {
		if (buffers.length > 0) {
			if (buffers[0] != null) {
				ByteBuf[] nativeBuffers = Arrays.stream(buffers)
						.map(b -> (/*(NettyDataBuffer)*/ b).getNativeBuffer())
						.toArray(ByteBuf[]::new);
				write(nativeBuffers);
			} else {
				ByteBuffer[] byteBuffers =
						Arrays.stream(buffers).map(NettyDataBuffer::asByteBuffer)
								.toArray(ByteBuffer[]::new);
				write(byteBuffers);
			}
		}
		return this;
	}

	public NettyDataBuffer write(ByteBuffer... buffers) {
		ByteBuf[] wrappedBuffers = Arrays.stream(buffers).map(Unpooled::wrappedBuffer)
				.toArray(ByteBuf[]::new);
		return write(wrappedBuffers);
	}

	/**
	 * Writes one or more Netty {@link io.netty.buffer.ByteBuf}s to this buffer, starting at the current writing
	 * position.
	 *
	 * @param byteBufs the buffers to write into this buffer
	 * @return this buffer
	 */
	public NettyDataBuffer write(ByteBuf... byteBufs) {
		CompositeByteBuf composite = new CompositeByteBuf(
				this.byteBuf.alloc(), this.byteBuf.isDirect(), byteBufs.length + 1);
		composite.addComponent(this.byteBuf);
		composite.addComponents(byteBufs);

		int writerIndex = this.byteBuf.readableBytes() +
				Arrays.stream(byteBufs).mapToInt(ByteBuf::readableBytes).sum();
		composite.writerIndex(writerIndex);

		this.byteBuf = composite;
		return this;
	}

	public NettyDataBuffer slice(int index, int length) {
		ByteBuf slice = this.byteBuf.slice(index, length);
		return new NettyDataBuffer(slice, this.dataBufferFactory);
	}

	public ByteBuffer asByteBuffer() {
		return this.byteBuf.nioBuffer();
	}

	public InputStream asInputStream() {
		return new ByteBufInputStream(this.byteBuf);
	}

	public OutputStream asOutputStream() {
		return new ByteBufOutputStream(this.byteBuf);
	}

	public NettyDataBuffer retain() {
		return new NettyDataBuffer(this.byteBuf.retain(), dataBufferFactory);
	}

	public boolean release() {
		return this.byteBuf.release();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NettyDataBuffer)) {
			return false;
		}
		NettyDataBuffer other = (NettyDataBuffer) obj;
		return this.byteBuf.equals(other.byteBuf);
	}

	@Override
	public int hashCode() {
		return this.byteBuf.hashCode();
	}

	@Override
	public String toString() {
		return this.byteBuf.toString();
	}

}
