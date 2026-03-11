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

package discord4j.voice;

import java.nio.ByteBuffer;

final class ResizableDirectByteBuffer {

    private ByteBuffer buffer;

    ResizableDirectByteBuffer(int initialCapacity) {
        this.buffer = ByteBuffer.allocateDirect(initialCapacity);
    }

    ByteBuffer write(byte[] bytes) {
        ensureCapacity(bytes.length);
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    ByteBuffer prepare(int capacity) {
        ensureCapacity(capacity);
        buffer.clear();
        return buffer;
    }

    byte[] read() {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }

    private void ensureCapacity(int capacity) {
        if (buffer.capacity() >= capacity) {
            return;
        }
        buffer = ByteBuffer.allocateDirect(capacity);
    }
}
