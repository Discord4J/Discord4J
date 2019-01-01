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
 * Used to receive audio.
 * <p>
 * The receiver uses a shared buffer. Keep this in mind when implementing.
 *
 * @see #receive()
 */
public abstract class AudioReceiver {

    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final AudioReceiver NO_OP = new AudioReceiver(ByteBuffer.allocate(0)) {
        @Override
        public void receive(char sequence, int timestamp, int ssrc, byte[] audio) {
        }
    };

    private final ByteBuffer buffer;

    public AudioReceiver() {
        this(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    public AudioReceiver(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Called when audio is received. After reading, the implementor is expected to clear the buffer.
     */
    public void receive() {
        getBuffer().get();
        getBuffer().get(); // skip first two bytes
        char sequence = getBuffer().getChar();
        int timestamp = getBuffer().getInt();
        int ssrc = getBuffer().getInt();
        byte[] audio = new byte[getBuffer().remaining()];
        getBuffer().get(audio);

        receive(sequence, timestamp, ssrc, audio);

        getBuffer().clear();
    }

    /**
     * Called when audio is received, automatically extracting useful information.
     * @param sequence The sequence of the packet.
     * @param timestamp The timestamp of the packet.
     * @param ssrc The ssrc of the audio source.
     * @param audio The <a href="https://en.wikipedia.org/wiki/Opus_(audio_format)">Opus</a>-encoded audio.
     */
    public abstract void receive(char sequence, int timestamp, int ssrc, byte[] audio);
}
