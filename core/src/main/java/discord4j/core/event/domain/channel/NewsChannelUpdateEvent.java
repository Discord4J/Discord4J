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
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link NewsChannel} is updated in a guild.
 * <p>
 * The {@link NewsChannel} may have been turned into a {@link TextChannel}.
 * <p>
 * The old news channel may not be present if news channels are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#channel-update">Channel Update</a>
 */
public class NewsChannelUpdateEvent extends ChannelEvent {

    private final GuildMessageChannel current;
    private final NewsChannel old;

    public NewsChannelUpdateEvent(DiscordClient client, GuildMessageChannel current, @Nullable NewsChannel old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link NewsChannel} that was updated in this event.
     * The {@link NewsChannel} may have been turned into a {@link TextChannel}.
     *
     * @return The current version of the updated {@link NewsChannel}.
     */
    public GuildMessageChannel getCurrent() {
        return current;
    }

    /**
     * Gets the current, new version of the {@link NewsChannel} that was updated in this event as an optional
     * value of a {@link NewsChannel}.
     *
     * @return An optional value of a {@link NewsChannel}. Empty if the channel was changed to a {@link TextChannel}.
     */
    public Optional<NewsChannel> getNewsChannel() {
        return current instanceof NewsChannel ? Optional.of((NewsChannel) current) : Optional.empty();
    }

    /**
     * Gets the current, new version of the {@link NewsChannel} that was updated in this event as an optional
     * value of a {@link TextChannel}.
     *
     * @return An optional value of a {@link TextChannel}. Empty if the channel continued to be a {@link NewsChannel}.
     */
    public Optional<TextChannel> getTextChannel() {
        return current instanceof TextChannel ? Optional.of((TextChannel) current) : Optional.empty();
    }

    /**
     * Gets the old version of the {@link NewsChannel} that was updated in this event, if present.
     * This may not be available if {@code NewsChannels} are not stored.
     *
     * @return The old version of the updated {@link NewsChannel}, if present.
     */
    public Optional<NewsChannel> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "NewsChannelUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
