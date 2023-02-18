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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.event.domain.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandGuildPermissions;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandPermissionsData;
import discord4j.gateway.ShardInfo;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Dispatched when an application command permission relevant to the current user is updated. This
 * event is dispatched by Discord.
 *
 * @see <a
 * href="https://discord.com/developers/docs/topics/gateway-events#application-commands">Application
 * Command Permissions Update</a>
 */
public class ApplicationCommandPermissionUpdateEvent extends ApplicationCommandEvent {

    private final ApplicationCommandGuildPermissions data;

    public ApplicationCommandPermissionUpdateEvent(GatewayDiscordClient gateway,
        ShardInfo shardInfo, ApplicationCommandGuildPermissions data) {
        super(gateway, shardInfo);
        this.data = data;
    }

    /**
     * Gets unique id of the command.
     *
     * @return The unique id of the command.
     */
    public Snowflake getId() {
        return data.getId();
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return data.getGuildId();
    }

    /**
     * Requests to retrieve the {@link Guild} that had an application command updated in this
     * event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in
     * the event. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(data.getGuildId());
    }

    /**
     * Gets the unique id of the parent application.
     *
     * @return The unique id of the parent application.
     */
    public Snowflake getApplicationId() {
        return data.getApplicationId();
    }

    /**
     * Get all permissions for this command on given guild
     *
     * @return Set permissions for this application command
     */
    public List<ApplicationCommandPermissionsData> getPermissions() {
        return data.getPermissions();
    }
}
