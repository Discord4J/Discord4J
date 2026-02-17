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

package discord4j.gateway;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A container to represent the current state of a Gateway session.
 */
public class SessionInfo {

    private final String id;
    private final int sequence;

    private SessionInfo(String id, int sequence) {
        this.id = Objects.requireNonNull(id);
        this.sequence = sequence;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static SessionInfo create(@JsonProperty("id") String id, @JsonProperty("sequence") int sequence) {
        return new SessionInfo(id, sequence);
    }

    /**
     * Return the identifier of the session represented by this {@link SessionInfo}.
     *
     * @return the session identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Return the sequence number of the session represented by this {@link SessionInfo}.
     *
     * @return the session sequence
     */
    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionInfo that = (SessionInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(sequence, that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sequence);
    }

    @Override
    public String toString() {
        return "SessionInfo{" +
                "id='" + id + '\'' +
                ", sequence=" + sequence +
                '}';
    }
}
