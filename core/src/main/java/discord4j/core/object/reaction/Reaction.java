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
package discord4j.core.object.reaction;

import discord4j.core.DiscordClient;
import discord4j.core.internal.ServiceMediator;
import discord4j.core.internal.data.stored.ReactionBean;
import discord4j.core.object.DiscordObject;

import java.util.Objects;

/**
 * A Discord message reaction.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#reaction-object">Reaction Object</a>
 */
public final class Reaction implements DiscordObject  {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final ReactionBean data;

    /**
     * Constructs a {@code Reaction} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Reaction(final ServiceMediator serviceMediator, final ReactionBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the number of people who reacted with this reaction's emoji.
     *
     * @return The number of people who reacted with this reaction's emoji.
     */
    public int getCount() {
        return data.getCount();
    }

    /**
     * Gets whether the current bot user reacted using this reaction's emoji.
     *
     * @return Whether the current bot user reacted using this reaction's emoji.
     */
    public boolean selfReacted() {
        return data.isMe();
    }

    /**
     * Gets this reaction's emoji.
     *
     * @return This reaction's emoji.
     */
    public ReactionEmoji getEmoji() {
        return ReactionEmoji.of(data.getEmojiId(), data.getEmojiName(), data.isEmojiAnimated());
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "data=" + data +
                '}';
    }
}
