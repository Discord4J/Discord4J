package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.PartialMessageData;

/**
 * A Partial Message used by the Forward Message feature.
 *
 * @see <a href="https://discord.com/developers/docs/resources/message#message-snapshot-object">
 * Message Snapshot Object</a>
 */
public class PartialMessage implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final PartialMessageData data;

    public PartialMessage(GatewayDiscordClient gateway, PartialMessageData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    public PartialMessageData getData() {
        return this.data;
    }


}
