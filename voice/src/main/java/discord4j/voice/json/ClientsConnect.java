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
package discord4j.voice.json;

import java.util.List;

public class ClientsConnect extends VoiceGatewayPayload<ClientsConnect.Data> {

    public static final int OP = 11;

    public ClientsConnect(List<String> userIds) {
        this(new Data(userIds));
    }

    public ClientsConnect(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final List<String> userIds;

        public Data(List<String> userIds) {
            this.userIds = userIds;
        }

        public List<String> getUserIds() {
            return userIds;
        }
    }
}
