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

import java.nio.ByteBuffer;

/**
 * Used to send audio.
 * <p>
 * The provider uses a shared buffer. Keep this in mind when implementing.
 *
 * @see #provide()
 */
public abstract class AudioProvider {

    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final AudioProvider NO_OP = new AudioProvider(ByteBuffer.allocate(0)) {
        @Override
        public boolean provide() {
            return false;
        }
    };

    private final ByteBuffer buffer;

    public AudioProvider() {
        this(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    public AudioProvider(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Called every 20 milliseconds and is expected to provide
     * <a href="https://en.wikipedia.org/wiki/Opus_(audio_format)">Opus</a>-encoded audio according to the format in
     * {@link Opus} by writing to the provider's {@link #getBuffer() buffer}.
     * @return Whether audio was provided (the buffer was mutated).
     */
    public abstract boolean provide();
}
