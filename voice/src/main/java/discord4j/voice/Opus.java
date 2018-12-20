package discord4j.voice;

public final class Opus {
    public static final int SAMPLE_RATE = 48_000; // Hz
    public static final int FRAME_TIME = 20; // ms
    public static final int FRAME_SIZE = SAMPLE_RATE / (1000 / FRAME_TIME); // 960
}
