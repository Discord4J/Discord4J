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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
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
