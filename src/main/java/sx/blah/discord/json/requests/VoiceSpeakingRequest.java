package sx.blah.discord.json.requests;

/**
 * Requests send to the server to tell it's about to receive audio, or telling it the client is going to stop sending audio
 */
public class VoiceSpeakingRequest {

    public int op = 5;
    public EventObject d;

    public VoiceSpeakingRequest(boolean speaking) {
        d = new EventObject(speaking);
    }

    public static class EventObject {

        public int delay = 0;
        public boolean speaking;

        public EventObject(boolean speaking) {
            this.speaking = speaking;
        }
    }
}
