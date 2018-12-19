package discord4j.voice.json;

public class SentSpeaking extends VoiceGatewayPayload<SentSpeaking.Data> {

    public SentSpeaking(boolean speaking, int delay, int ssrc) {
        super(5, new Data(speaking, delay, ssrc));
    }

    public static class Data {

        public boolean speaking;
        public int delay;
        public int ssrc;

        public Data(boolean speaking, int delay, int ssrc) {
            this.speaking = speaking;
            this.delay = delay;
            this.ssrc = ssrc;
        }
    }
}
