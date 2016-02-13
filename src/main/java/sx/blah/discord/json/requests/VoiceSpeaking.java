package sx.blah.discord.json.requests;

public class VoiceSpeaking {

    public int op = 5;
    public EventObject d;

    public VoiceSpeaking(boolean speaking) {
        d = new EventObject(speaking);
    }

    public class EventObject {

        public int delay = 0;
        public boolean speaking;

        public EventObject(boolean speaking) {
            this.speaking = speaking;
        }
    }
}
