package sx.blah.discord.json.requests;

public class VoiceChannelConnectRequest {

    /**
     * The opcode, always 4
     */
    public int op = 4;

    /**
     * The event object
     */
    public EventObject d;

    public VoiceChannelConnectRequest(String guild_id, String channel_id, boolean self_mute, boolean self_deaf) {
        d = new EventObject(guild_id, channel_id, self_mute, self_deaf);
    }

    /**
     * The event object for this operation
     */
    public class EventObject {

        private final String guild_id;
        private final String channel_id;
        private final boolean self_mute;
        private final boolean self_deaf;

        public EventObject(String guild_id, String channel_id, boolean self_mute, boolean self_deaf) {
            this.guild_id = guild_id;
            this.channel_id = channel_id;
            this.self_mute = self_mute;
            this.self_deaf = self_deaf;
        }
    }
}
