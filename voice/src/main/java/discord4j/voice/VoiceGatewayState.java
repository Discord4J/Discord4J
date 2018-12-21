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
import reactor.core.publisher.MonoSink;

class VoiceGatewayState {

    static final class WaitingForHello extends VoiceGatewayState {

        final Disposable websocketTask;
        final Runnable connectedCallback;

        WaitingForHello(Disposable websocketTask, Runnable connectedCallback) {
            this.websocketTask = websocketTask;
            this.connectedCallback = connectedCallback;
        }
    }

    static final class WaitingForReady extends VoiceGatewayState {

        final Disposable websocketTask;
        final Runnable connectedCallback;
        final Disposable heartbeatTask;

        WaitingForReady(Disposable websocketTask, Runnable connectedCallback,
                               Disposable heartbeatTask) {
            this.websocketTask = websocketTask;
            this.connectedCallback = connectedCallback;
            this.heartbeatTask = heartbeatTask;
        }
    }

    static final class WaitingForSessionDescription extends VoiceGatewayState {

        final Disposable websocketTask;
        final Runnable connectedCallback;
        final Disposable heartbeatTask;
        final int ssrc;

        WaitingForSessionDescription(Disposable websocketTask, Runnable connectedCallback,
                                     Disposable heartbeatTask, int ssrc) {
            this.websocketTask = websocketTask;
            this.connectedCallback = connectedCallback;
            this.heartbeatTask = heartbeatTask;
            this.ssrc = ssrc;
        }
    }

    static final class ReceivingEvents extends VoiceGatewayState {

        final Disposable websocketTask;
        final Runnable connectedCallback;
        final Disposable heartbeatTask;
        final int ssrc;
        final byte[] secretKey;
        final Disposable sendingTask;

        ReceivingEvents(Disposable websocketTask, Runnable connectedCallback,
                        Disposable heartbeatTask, int ssrc, byte[] secretKey, Disposable sendingTask) {
            this.websocketTask = websocketTask;
            this.connectedCallback = connectedCallback;
            this.heartbeatTask = heartbeatTask;
            this.ssrc = ssrc;
            this.secretKey = secretKey;
            this.sendingTask = sendingTask;
        }
    }

    static final class Stopped extends VoiceGatewayState {
        static final Stopped INSTANCE = new Stopped();
        private Stopped() {}
    }

    private VoiceGatewayState() {}
}
