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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.TextChannel;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link TextChannel} is updated in a guild.
 * <p>
 * The old text channel may not be present if text channels are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#channel-update">Channel Update</a>
 */
public class TextChannelUpdateEvent extends ChannelEvent {

    private final TextChannel current;
    private final TextChannel old;

    public TextChannelUpdateEvent(DiscordClient client, TextChannel current, @Nullable TextChannel old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link TextChannel} that was updated in this event.
     *
     * @return The current version of the updated {@link TextChannel}.
     */
    public TextChannel getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link TextChannel} that was updated in this event, if present.
     * This may not be available if {@code TextChannels} are not stored.
     *
     * @return The old version of the updated {@link TextChannel}, if present.
     */
    public Optional<TextChannel> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "TextChannelUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
