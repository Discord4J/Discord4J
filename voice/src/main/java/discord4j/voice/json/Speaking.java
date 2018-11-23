package discord4j.voice.json;

public class Speaking extends VoiceGatewayPayload<Speaking.Data> {

    public static final int OP = 5;

    public Speaking(String user_id, int ssrc, boolean speaking) {
        this(new Data(user_id, ssrc, speaking));
    }

    public Speaking(Data data) {
        super(OP, data);
    }


    public static class Data {

        private final String user_id;
        private final int ssrc;
        private final boolean speaking;

        public Data(String user_id, int ssrc, boolean speaking) {
            this.user_id = user_id;
            this.ssrc = ssrc;
            this.speaking = speaking;
        }
    }
}
