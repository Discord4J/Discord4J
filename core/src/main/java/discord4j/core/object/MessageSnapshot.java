package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageSnapshotsData;

/**
 * Represent the message associated with the {@link Message#getMessageReference()}.
 * This is a minimal subset of fields in a message.
 *
 * @see <a href="https://discord.com/developers/docs/resources/message#message-snapshot-object">
 * Message Snapshot Object</a>
 */
public class MessageSnapshot implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageSnapshotsData data;

    public MessageSnapshot(GatewayDiscordClient gateway, MessageSnapshotsData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Returns a partial message with minimal subset of fields in the forwarded message.
     *
     * @return A partial message with minimal subset of fields in the forwarded message.
     */
    public PartialMessage getMessage() {
        return new PartialMessage(this.gateway, this.data.message());
    }
}
