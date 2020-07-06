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

public class SelectProtocol extends VoiceGatewayPayload<SelectProtocol.Data> {

    public static final int OP = 1;

    public SelectProtocol(String protocol, String address, int port, String mode) {
        this(new Data(protocol, new Data.Inner(address, port, mode)));
    }

    public SelectProtocol(Data data) {
        super(OP, data);
    }

    public static class Data {

        private final String protocol;
        private final Inner data;

        public Data(String protocol, Inner data) {
            this.protocol = protocol;
            this.data = data;
        }

        public String getProtocol() {
            return protocol;
        }

        public Inner getData() {
            return data;
        }

        public static class Inner {

            private final String address;
            private final int port;
            private final String mode;

            public Inner(String address, int port, String mode) {
                this.address = address;
                this.port = port;
                this.mode = mode;
            }

            public String getAddress() {
                return address;
            }

            public int getPort() {
                return port;
            }

            public String getMode() {
                return mode;
            }
        }
    }
}
