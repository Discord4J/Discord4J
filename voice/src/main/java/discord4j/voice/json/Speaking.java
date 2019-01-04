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

public class Speaking extends VoiceGatewayPayload<Speaking.Data> {

    public static final int OP = 5;

    public Speaking(String userId, int ssrc, boolean speaking) {
        this(new Data(userId, ssrc, speaking));
    }

    public Speaking(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final String userId;
        private final int ssrc;
        private final boolean speaking;

        public Data(String userId, int ssrc, boolean speaking) {
            this.userId = userId;
            this.ssrc = ssrc;
            this.speaking = speaking;
        }
    }
}
