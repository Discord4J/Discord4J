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

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Ready extends VoiceGatewayPayload<Ready.Data> {

    public static final int OP = 2;

    public Ready(int ssrc, String ip, int port) {
        this(ssrc, ip, port, Collections.<String>emptyList(), null);
    }

    public Ready(int ssrc, String ip, int port, List<String> modes) {
        this(ssrc, ip, port, modes, null);
    }

    public Ready(int ssrc, String ip, int port, List<String> modes, @Nullable String authSessionId) {
        this(new Data(ssrc, ip, port, modes, authSessionId));
    }

    public Ready(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final int ssrc;
        private final String ip;
        private final int port;
        private final List<String> modes;
        private final @Nullable String authSessionId;

        public Data(int ssrc, String ip, int port, List<String> modes, @Nullable String authSessionId) {
            this.ssrc = ssrc;
            this.ip = ip;
            this.port = port;
            this.modes = modes;
            this.authSessionId = authSessionId;
        }

        public int getSsrc() {
            return ssrc;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public List<String> getModes() {
            return modes;
        }

        public @Nullable String getAuthSessionId() {
            return authSessionId;
        }
    }
}
