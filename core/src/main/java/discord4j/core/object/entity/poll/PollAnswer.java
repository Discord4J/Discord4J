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
import discord4j.discordjson.json.PollAnswerObject;
import discord4j.discordjson.json.PollMediaObject;

import java.util.Optional;

/**
 * An answer in a poll.
 *
 * @see <a href="https://discord.com/developers/docs/resources/poll#poll-answer-object">Poll Answer Object</a>
 */
public class PollAnswer {

    private final PollAnswerObject data;

    /**
     * Constructs a PollAnswer object.
     *
     * @param data the poll answer object
     */
    PollAnswer(PollAnswerObject data) {
        this.data = data;
    }

    /**
     * Creates a poll answer with the given text.
     *
     * @param text the text of the poll answer
     * @return the poll answer
     */
    public static PollAnswer of(String text) {
        return new PollAnswer(PollAnswerObject.builder().data(PollMediaObject.builder().text(text).build()).build());
    }

    /**
     * Creates a poll answer with the given emoji.
     *
     * @param emoji the emoji of the poll answer
     * @return the poll answer
     */
    public static PollAnswer of(String text, ReactionEmoji emoji) {
        return new PollAnswer(PollAnswerObject.builder().data(PollMediaObject.builder().text(text).emoji(emoji.asEmojiData()).build()).build());
    }

    /**
     * Gets the raw data of the poll answer.
     *
     * @return the raw data of the poll answer
     */
    public PollAnswerObject getData() {
        return this.data;
    }

    /**
     * Gets the answer id of this poll answer.
     *
     * @return the answer id of this poll answer
     */
    public Integer getAnswerId() {
        return this.data.answerId().get(); // We can safely call get() here because the answerId is always present
    }

    /**
     * Gets the text of the poll answer.
     *
     * @return An {@link Optional} containing the text of the poll answer, or {@link Optional#empty()} if not present
     */
    public Optional<String> getText() {
        return this.data.data().text().toOptional();
    }

    /**
     * Gets the emoji of the poll answer.
     *
     * @return An {@link Optional} containing the emoji of the poll answer, or {@link Optional#empty()} if not present
     */
    public Optional<ReactionEmoji> getEmoji() {
        return this.data.data().emoji().toOptional().map(ReactionEmoji::of);
    }

}
