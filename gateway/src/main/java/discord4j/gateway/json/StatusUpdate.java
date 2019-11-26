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
package discord4j.gateway.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import reactor.util.annotation.Nullable;

public class StatusUpdate implements PayloadData {

    @Nullable
    private final Long since;
    @Nullable
    private final Game game;
    private final String status;
    private final boolean afk;

    @JsonCreator
    public StatusUpdate(@JsonProperty("since") @Nullable Long since,
                        @JsonProperty("game") @Nullable Game game,
                        @JsonProperty("status") String status,
                        @JsonProperty("afk") boolean afk) {
        this.since = since;
        this.game = game;
        this.status = status;
        this.afk = afk;
    }

    public StatusUpdate(@Nullable Game game, String status) {
        this.since = null;
        this.game = game;
        this.status = status;
        this.afk = false;
    }

    @Override
    public String toString() {
        return "StatusUpdate{" +
                "since=" + since +
                ", game=" + game +
                ", status='" + status + '\'' +
                ", afk=" + afk +
                '}';
    }

    @PossibleJson
    public static class Game {

        private final String name;
        private final int type;
        private final Possible<String> url;

        public Game(String name, int type, Possible<String> url) {
            this.name = name;
            this.type = type;
            this.url = url;
        }

        @Override
        public String toString() {
            return "Game{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", url=" + url +
                    '}';
        }
    }
}
