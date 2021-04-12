package discord4j.core.event.domain.integration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when an integration is deleted.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway#integration-delete">Integration Delete</a>
 */
public class IntegrationDeleteEvent extends Event {

    private final long id;
    private final long guildId;
    @Nullable
    private final Long applicationId;

    public IntegrationDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long id, long guildId,
                                  @Nullable Long applicationId) {
        super(gateway, shardInfo);
        this.id = id;
        this.guildId = guildId;
        this.applicationId = applicationId;
    }

    /**
     * Gets the id of the integration.
     *
     * @return The id of the integration.
     */
    public Snowflake getId() {
        return Snowflake.of(id);
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
     * Gets the id of the bot/OAuth2 application for this discord integration, if present.
     *
     * @return The id of the bot/OAuth2 application for this discord integration, if present.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(applicationId).map(Snowflake::of);
    }

}
