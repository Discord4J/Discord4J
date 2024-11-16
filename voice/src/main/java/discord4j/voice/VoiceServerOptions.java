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

package discord4j.voice;

/**
 * A set of options required to identify a voice server, for identifying.
 */
public class VoiceServerOptions {

    private static final int AUDIO_GATEWAY_VERSION = 4;

    private final String token;
    private final String endpoint;

    public VoiceServerOptions(String token, String endpoint) {
        this.token = token;
        this.endpoint = "wss://" + endpoint + "?v=" + AUDIO_GATEWAY_VERSION;
    }

    public String getToken() {
        return token;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
