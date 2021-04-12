package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.IntegrationData;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * A Discord integration.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#integration-object">Integration Resource</a>
 */
public class Integration implements Entity {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final IntegrationData data;

    /**
     * Constructs an {@code Integration} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Integration(final GatewayDiscordClient gateway, final IntegrationData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the integration.
     *
     * @return The data of the integration.
     */
    public IntegrationData getData() {
        return data;
    }

    /* TODO: Implements getter */

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "Integration{" +
            "data=" + data +
            '}';
    }

}
