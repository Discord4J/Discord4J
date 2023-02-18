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

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandPermissionsData;
import discord4j.discordjson.json.GuildApplicationCommandPermissionsData;
import java.util.List;
import java.util.Objects;

/**
 * A Discord application command.
 *
 * @see <a
 * href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommand">
 * Application Command Object</a>
 */
@Experimental
public class ApplicationCommandGuildPermissions implements DiscordObject {

    /**
     * The maximum amount of permission overrides for this command.
     */
    public static final int MAX_PERMISSION_ENTRIES = 100;

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final GuildApplicationCommandPermissionsData data;

    /**
     * Constructs an {@code ApplicationCommandGuildPermissions} with an associated
     * {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data    The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandGuildPermissions(final GatewayDiscordClient gateway,
        final GuildApplicationCommandPermissionsData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets unique id of the command.
     *
     * @return The unique id of the command.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }


    /**
     * Gets the id of the guild if the command is guild scoped.
     *
     * @return The id of the guild
     */
    public Snowflake getGuildId() {
        return Snowflake.of(data.guildId());
    }

    /**
     * Gets the unique id of the parent application.
     *
     * @return The unique id of the parent application.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(data.applicationId());
    }

    /**
     * Gets the name of the command.
     *
     * @return The name of the command.
     */
    public List<ApplicationCommandPermissionsData> getPermissions() {
        return data.permissions();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
