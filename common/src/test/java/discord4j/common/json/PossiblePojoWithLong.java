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
package discord4j.common.json;

import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.PossibleLong;
import reactor.util.annotation.Nullable;

@PossibleJson
public class PossiblePojoWithLong {

    @Nullable
    private Possible<String> string;
    @Nullable
    private PossibleLong llong;

    public PossiblePojoWithLong(@Nullable Possible<String> string, @Nullable PossibleLong llong) {
        this.string = string;
        this.llong = llong;
    }

    @Nullable
    public Possible<String> getString() {
        return string;
    }

    public void setString(@Nullable Possible<String> string) {
        this.string = string;
    }

    @Nullable
    public PossibleLong getLlong() {
        return llong;
    }

    public void setLlong(@Nullable PossibleLong llong) {
        this.llong = llong;
    }
}
