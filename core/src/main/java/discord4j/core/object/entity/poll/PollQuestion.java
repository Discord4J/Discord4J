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
package discord4j.core.object.entity.poll;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.PollMediaObject;

import java.util.Optional;

/**
 * A question in a poll.
 *
 * @see <a href="https://discord.com/developers/docs/resources/poll#poll-media-object">Poll Media Object</a>
 */
public class PollQuestion {

    private final PollMediaObject data;

    /**
     * Constructs a PollQuestion object.
     *
     * @param data the poll media object
     */
    PollQuestion(final PollMediaObject data) {
        this.data = data;
    }

    /**
     * Gets the raw data of the poll question.
     *
     * @return the raw data of the poll question
     */
    public PollMediaObject getData() {
        return this.data;
    }

    /**
     * Gets the text of the poll question.
     *
     * @return An {@link Optional} containing the text of the poll question, or {@link Optional#empty()} if not present
     */
    public Optional<String> getText() {
        return this.data.text().toOptional();
    }

    /**
     * Gets the emoji of the poll question.
     *
     * @return An {@link Optional} containing the emoji of the poll question, or {@link Optional#empty()} if not present
     */
    public Optional<ReactionEmoji> getEmoji() {
        return this.data.emoji().toOptional().map(ReactionEmoji::of);
    }

}
