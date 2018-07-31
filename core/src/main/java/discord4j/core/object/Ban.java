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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.BanBean;
import discord4j.core.object.entity.User;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord ban.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#ban-object">Ban Object</a>
 */
public final class Ban implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final BanBean data;

    /**
     * Constructs a {@code Ban} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Ban(final ServiceMediator serviceMediator, final BanBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the reason for the ban.
     *
     * @return The reason for the ban.
     */
    public Optional<String> getReason() {
        return Optional.ofNullable(data.getReason());
    }

    /**
     * Gets the banned user.
     *
     * @return The banned user.
     */
    public User getUser() {
        return new User(serviceMediator, data.getUser());
    }
}
