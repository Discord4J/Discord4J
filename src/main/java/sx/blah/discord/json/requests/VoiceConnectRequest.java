package sx.blah.discord.json.requests;

/**
 *
 */
public class VoiceConnectRequest {

    /**
     * The opcode, always 4
     */
    public int op = 0;

    /**
     * The event object
     */
    public EventObject d;

    public VoiceConnectRequest(String server_id, String user_id, String session_id, String token) {
        d = new EventObject(server_id, user_id, session_id, token);
    }

    /**
     * The event object for this operation
     */
    public static class EventObject {

        private final String server_id;
        private final String user_id;
        private final String session_id;
        private final String token;

        public EventObject(String server_id, String user_id, String session_id, String token) {
            this.server_id = server_id;
            this.user_id = user_id;
            this.session_id = session_id;
            this.token = token;
        }
    }
}
