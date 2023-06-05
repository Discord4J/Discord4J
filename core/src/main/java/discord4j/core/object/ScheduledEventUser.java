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

package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.GuildScheduledEventUserData;

import java.util.Objects;
import java.util.Optional;

public class ScheduledEventUser implements DiscordObject {

    /** The gateway associated with this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final GuildScheduledEventUserData data;

    /** The ID of the guild the event belongs to. */
    private final Snowflake guildId;

    /**
     * Constructs a {@code ScheduledEventUser} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated with this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild the event belongs to, must be non-null.
     */
    public ScheduledEventUser(GatewayDiscordClient gateway, GuildScheduledEventUserData data, Snowflake guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = Objects.requireNonNull(guildId);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the ID of the event the user is subscribed to.
     *
     * @return The ID of the event the user is subscribed to.
     */
    public Snowflake getEventId() {
        return Snowflake.of(data.guildScheduledEventId());
    }

    /**
     * Gets the {@link User} subscribed to the event.
     *
     * @return The {@code User} subscribed to the event.
     */
    public User getUser() {
        return new User(gateway, data.user());
    }

    /**
     * Gets the {@link PartialMember}, if the user is a member of the guild the event is belongs to.
     *
     * @return The {@code PartialMember}, if the user is a member of the guild the event is belongs to.
     */
    public Optional<PartialMember> getMember() {
        return data.member().toOptional()
            .map(memberData -> new PartialMember(gateway, data.user(), memberData, guildId.asLong()));
    }

    @Override
    public String toString() {
        return "ScheduledEventUser{"+
            "data=" + data +
            '}';
    }
}
