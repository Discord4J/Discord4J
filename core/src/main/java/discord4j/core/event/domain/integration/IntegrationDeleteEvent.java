/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.domain.integration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

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
