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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

/**
 * Implementation of the {@code DataBufferFactory} interface based on a Netty {@link io.netty.buffer.ByteBufAllocator}.
 *
 * @author Arjen Poutsma
 * @see io.netty.buffer.PooledByteBufAllocator
 * @see io.netty.buffer.UnpooledByteBufAllocator
 */
public class NettyDataBufferFactory {

	private final ByteBufAllocator byteBufAllocator;


	/**
	 * Creates a new {@code NettyDataBufferFactory} based on the given factory.
	 *
	 * @param byteBufAllocator the factory to use
	 * @see io.netty.buffer.PooledByteBufAllocator
	 * @see io.netty.buffer.UnpooledByteBufAllocator
	 */
	public NettyDataBufferFactory(ByteBufAllocator byteBufAllocator) {
		this.byteBufAllocator = byteBufAllocator;
	}

	/**
	 * Return the {@code ByteBufAllocator} used by this factory.
	 */
	public ByteBufAllocator getByteBufAllocator() {
		return this.byteBufAllocator;
	}

	public NettyDataBuffer allocateBuffer() {
		ByteBuf byteBuf = this.byteBufAllocator.buffer();
		return new NettyDataBuffer(byteBuf, this);
	}

	public NettyDataBuffer allocateBuffer(int initialCapacity) {
		ByteBuf byteBuf = this.byteBufAllocator.buffer(initialCapacity);
		return new NettyDataBuffer(byteBuf, this);
	}

	public NettyDataBuffer wrap(ByteBuffer byteBuffer) {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
		return new NettyDataBuffer(byteBuf, this);
	}

	public NettyDataBuffer wrap(byte[] bytes) {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
		return new NettyDataBuffer(byteBuf, this);
	}

	/**
	 * Wrap the given Netty {@link io.netty.buffer.ByteBuf} in a {@code NettyDataBuffer}.
	 *
	 * @param byteBuf the Netty byte buffer to wrap
	 * @return the wrapped buffer
	 */
	public NettyDataBuffer wrap(ByteBuf byteBuf) {
		return new NettyDataBuffer(byteBuf, this);
	}

	/**
	 * Return the given Netty {@link NettyDataBuffer} as a {@link io.netty.buffer.ByteBuf}. Returns the {@linkplain
	 * NettyDataBuffer#getNativeBuffer() native buffer} if {@code buffer} is a {@link NettyDataBuffer}; returns {@link
	 * io.netty.buffer.Unpooled#wrappedBuffer(java.nio.ByteBuffer)} otherwise.
	 *
	 * @param buffer the {@code DataBuffer} to return a {@code ByteBuf} for.
	 * @return the netty {@code ByteBuf}
	 */
	public static ByteBuf toByteBuf(NettyDataBuffer buffer) {
		//        if (buffer instanceof NettyDataBuffer) {
		//            return ((NettyDataBuffer) buffer).getNativeBuffer();
		//        }
		//        else {
		//            return Unpooled.wrappedBuffer(buffer.asByteBuffer());
		//        }
		return buffer.getNativeBuffer();
	}

	@Override
	public String toString() {
		return "NettyDataBufferFactory (" + this.byteBufAllocator + ")";
	}
}
