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
package discord4j.voice.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Identify extends VoiceGatewayPayload<Identify.Data> {

    public static final int OP = 0;

    public Identify(String serverId, String userId, String sessionId, String token) {
        this(new Data(serverId, userId, sessionId, token));
    }

    public Identify(Data data) {
        super(OP, data);
    }

    public static class Data {

        @JsonProperty("server_id")
        public String serverId;
        @JsonProperty("user_id")
        public String userId;
        @JsonProperty("session_id")
        public String sessionId;
        public String token;

        public Data(String serverId, String userId, String sessionId, String token) {
            this.serverId = serverId;
            this.userId = userId;
            this.sessionId = sessionId;
            this.token = token;
        }
    }
}
