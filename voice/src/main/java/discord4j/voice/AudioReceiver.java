package discord4j.voice;

import java.nio.ByteBuffer;

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

    public abstract void receive(char sequence, int timestamp, int ssrc, byte[] audio);
}
