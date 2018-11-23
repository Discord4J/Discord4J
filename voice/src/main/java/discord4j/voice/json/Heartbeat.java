package discord4j.voice.json;

public class Heartbeat extends VoiceGatewayPayload<Long> {

    public static final int OP = 3;

    public Heartbeat(long data) {
        super(OP, data);
    }
}
