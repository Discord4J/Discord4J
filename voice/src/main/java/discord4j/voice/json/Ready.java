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

public class Ready extends VoiceGatewayPayload<Ready.Data> {

    public static final int OP = 2;

    public Ready(int ssrc, String ip, int port) {
        this(new Data(ssrc, ip, port));
    }

    public Ready(Data data) {
        super(OP, data);
    }

    public static class Data {

        public int ssrc;
        public String ip;
        public int port;

        public Data(int ssrc, String ip, int port) {
            this.ssrc = ssrc;
            this.ip = ip;
            this.port = port;
        }
    }
}
