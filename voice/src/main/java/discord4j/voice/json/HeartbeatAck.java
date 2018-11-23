package discord4j.voice.json;

public class HeartbeatAck extends VoiceGatewayPayload<Long> {

    public static final int OP = 6;

    public HeartbeatAck(long data) {
        super(OP, data);
    }
}
