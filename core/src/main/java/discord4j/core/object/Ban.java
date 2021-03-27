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
package discord4j.core.object;

import discord4j.discordjson.json.BanData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord ban.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#ban-object">Ban Object</a>
 */
public final class Ban implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final BanData data;

    /**
     * Constructs a {@code Ban} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Ban(final GatewayDiscordClient gateway, final BanData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the ban.
     *
     * @return The data of the ban.
     */
    public BanData getData() {
        return data;
    }

    /**
     * Gets the reason for the ban.
     *
     * @return The reason for the ban.
     */
    public Optional<String> getReason() {
        return data.reason();
    }

    /**
     * Gets the banned user.
     *
     * @return The banned user.
     */
    public User getUser() {
        return new User(gateway, data.user());
    }

    @Override
    public String toString() {
        return "Ban{" +
                "data=" + data +
                '}';
    }
}
