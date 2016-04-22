package sx.blah.discord.json.requests;

public class VoiceUDPConnectRequest {

    /**
     * The opcode, always 4
     */
    public int op = 1;

    /**
     * The event object
     */
    public EventObject d;

    public VoiceUDPConnectRequest(String address, int port) {
        d = new EventObject(address, port);
    }

    /**
     * The event object for this operation
     */
    public class EventObject {

        private final String protocol = "udp";
        private EventData data;

        public EventObject(String address, int port) {
            this.data = new EventData(address, port);
        }
    }

    private static class EventData {
        private final String address;
        private final int port;
        private final String mode = "xsalsa20_poly1305";

        public EventData(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}
