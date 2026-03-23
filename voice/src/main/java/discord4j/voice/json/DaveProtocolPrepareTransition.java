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

import com.fasterxml.jackson.annotation.JsonProperty;

public class DaveProtocolPrepareTransition extends VoiceGatewayPayload<DaveProtocolPrepareTransition.Data> {

    public static final int OP = 21;

    public DaveProtocolPrepareTransition(int transitionId, int protocolVersion) {
        this(new Data(transitionId, protocolVersion));
    }

    public DaveProtocolPrepareTransition(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final int transitionId;
        private final int protocolVersion;

        public Data(int transitionId, int protocolVersion) {
            this.transitionId = transitionId;
            this.protocolVersion = protocolVersion;
        }

        @JsonProperty("transition_id")
        public int getTransitionId() {
            return transitionId;
        }

        @JsonProperty("protocol_version")
        public int getProtocolVersion() {
            return protocolVersion;
        }
    }
}
