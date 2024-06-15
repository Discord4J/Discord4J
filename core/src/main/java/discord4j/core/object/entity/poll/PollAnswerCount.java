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

import discord4j.discordjson.json.PollAnswerCountObject;

/**
 * An answer count in a poll.
 *
 * @see <a href="https://discord.com/developers/docs/resources/poll#poll-results-object-poll-answer-count-object-structure">Poll Answer Count Object</a>
 */
public class PollAnswerCount {

    private final PollAnswerCountObject data;

    PollAnswerCount(PollAnswerCountObject data) {
        this.data = data;
    }

    /**
     * Gets the raw data of the poll answer count.
     *
     * @return the raw data of the poll answer count
     */
    public PollAnswerCountObject getData() {
        return this.data;
    }

    /**
     * Gets the answer id of this poll answer count.
     *
     * @return the answer id of this poll answer count
     */
    public int getAnswerId() {
        return this.data.id();
    }

    /**
     * Gets the count of this poll answer count.
     *
     * @return the count of this poll answer count
     */
    public int getCount() {
        return this.data.count();
    }

}
