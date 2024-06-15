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

import discord4j.discordjson.json.PollResultObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A poll result.
 *
 * @see <a href="https://discord.com/developers/docs/resources/poll#poll-results-object">Poll Result Object</a>
 */
public class PollResult {

    private final PollResultObject data;
    private final List<PollAnswerCount> answerCount;
    private final Map<Integer, PollAnswerCount> answerCountById;

    PollResult(PollResultObject data) {
        this.data = data;

        this.answerCount = data.answerCounts().stream().map(PollAnswerCount::new).collect(Collectors.toList());
        this.answerCountById = this.answerCount.stream().collect(Collectors.toMap(PollAnswerCount::getAnswerId, count -> count));
    }

    /**
     * Gets the raw data of the poll result.
     *
     * @return the raw data of the poll result
     */
    public PollResultObject getData() {
        return this.data;
    }

    /**
     * Gets if the poll result is finalized.
     *
     * @return {@code true} if the poll results are finalized, {@code false} otherwise
     */
    public boolean isFinalized() {
        return this.data.isFinalized();
    }

    /**
     * Gets the answer counts of the poll result.
     *
     * @return the answer counts of the poll result
     */
    public List<PollAnswerCount> getAnswerCount() {
        return this.answerCount;
    }

    /**
     * Gets the answer count of the poll result by answer id.
     *
     * @param answerId the answer id
     * @return An {@link Optional} containing the answer count of the poll result by answer id, or
     * {@link Optional#empty()} if the answer id is not present
     */
    public Optional<Integer> getAnswerCountById(int answerId) {
        return Optional.ofNullable(this.answerCountById.get(answerId)).map(PollAnswerCount::getCount);
    }

}
