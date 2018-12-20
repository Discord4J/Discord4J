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
