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
package discord4j.common.json;

import discord4j.common.jackson.Possible;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public class PossiblePojo {

    private Possible<String> string = Possible.absent();

    public PossiblePojo(@Nullable Possible<String> string) {
        this.string = string;
    }

    public PossiblePojo() {
    }

    @Nullable
    public Possible<String> getString() {
        return string;
    }

    @Override
    public String toString() {
        return "PossiblePojo{" +
                "string=" + string +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PossiblePojo that = (PossiblePojo) o;
        return Objects.equals(string, that.string);
    }
}
