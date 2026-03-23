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

public class DaveProtocolPrepareEpoch extends VoiceGatewayPayload<DaveProtocolPrepareEpoch.Data> {

    public static final int OP = 24;

    public DaveProtocolPrepareEpoch(long epoch, int protocolVersion) {
        this(new Data(epoch, protocolVersion));
    }

    public DaveProtocolPrepareEpoch(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final long epoch;
        private final int protocolVersion;

        public Data(long epoch, int protocolVersion) {
            this.epoch = epoch;
            this.protocolVersion = protocolVersion;
        }

        public long getEpoch() {
            return epoch;
        }

        public int getProtocolVersion() {
            return protocolVersion;
        }
    }
}
