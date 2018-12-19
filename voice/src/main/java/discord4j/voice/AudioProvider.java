package discord4j.voice;

import java.nio.ByteBuffer;

public interface AudioProvider {

    boolean provide(ByteBuffer buf);
}
