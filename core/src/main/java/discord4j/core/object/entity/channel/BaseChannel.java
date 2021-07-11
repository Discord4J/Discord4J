package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ChannelData;
import discord4j.rest.entity.RestChannel;

import java.util.Objects;

/** An internal implementation of {@link Channel} designed to streamline inheritance. */
class BaseChannel implements Channel {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ChannelData data;

    /** A handle to execute REST API operations for this entity. */
    private final RestChannel rest;

    /**
     * Constructs a {@code BaseChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.rest = RestChannel.create(gateway.getRestClient(), Snowflake.of(data.id()));
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public final RestChannel getRestChannel() {
        return rest;
    }

    @Override
    public final ChannelData getData() {
        return data;
    }
}
