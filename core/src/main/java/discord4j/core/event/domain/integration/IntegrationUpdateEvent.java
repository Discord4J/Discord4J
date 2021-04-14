package discord4j.core.event.domain.integration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Integration;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when an integration is updated.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway#integration-update">Integration Update</a>
 */
public class IntegrationUpdateEvent extends Event {

    private final long guildId;
    private final Integration integration;

    public IntegrationUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId,
                                  Integration integration) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.integration = integration;
    }

    /**
     * Gets the id of the guild.
     *
     * @return The id of the guild.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the updated integration.
     *
     * @return The updated integration.
     */
    public Integration getIntegration() {
        return integration;
    }

}
