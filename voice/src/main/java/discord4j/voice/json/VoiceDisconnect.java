package discord4j.voice.json;

public class VoiceDisconnect extends VoiceGatewayPayload<VoiceDisconnect.Data> {

    public static final int OP = 13;

    public VoiceDisconnect(String user_id) {
        this(new Data(user_id));
    }

    public VoiceDisconnect(Data data) {
        super(OP, data);
    }

    public static class Data {

        public final String user_id;

        public Data(String user_id) {
            this.user_id = user_id;
        }
    }
}
