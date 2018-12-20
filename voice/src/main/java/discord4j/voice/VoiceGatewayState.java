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
package discord4j.voice;

import reactor.core.Disposable;

class VoiceGatewayState {

    static final class WaitingForHello extends VoiceGatewayState {
        static final WaitingForHello INSTANCE = new WaitingForHello();
        private WaitingForHello() {}
    }

    static final class WaitingForReady extends VoiceGatewayState {
        private final Disposable heartbeat;

        WaitingForReady(Disposable heartbeat) {
            this.heartbeat = heartbeat;
        }

        Disposable getHeartbeat() {
            return heartbeat;
        }
    }

    static final class WaitingForSessionDescription extends VoiceGatewayState {
        private final Disposable heartbeat;
        private final int ssrc;

        WaitingForSessionDescription(Disposable heartbeat, int ssrc) {
            this.heartbeat = heartbeat;
            this.ssrc = ssrc;
        }

        Disposable getHeartbeat() {
            return heartbeat;
        }

        int getSsrc() {
            return ssrc;
        }
    }

    static final class ReceivingEvents extends VoiceGatewayState {
        private final Disposable heartbeat;
        private final int ssrc;
        private final byte[] secretKey;
        private final Disposable sending;

        ReceivingEvents(Disposable heartbeat, int ssrc, byte[] secretKey, Disposable sending) {
            this.heartbeat = heartbeat;
            this.ssrc = ssrc;
            this.secretKey = secretKey;
            this.sending = sending;
        }

        Disposable getHeartbeat() {
            return heartbeat;
        }

        int getSsrc() {
            return ssrc;
        }

        byte[] getSecretKey() {
            return secretKey;
        }

        Disposable getSending() {
            return sending;
        }
    }

    private VoiceGatewayState() {}
}
