package discord4j.voice;

import reactor.core.Disposable;

class VoiceGatewayState {

    static final class WaitingForHello extends VoiceGatewayState {
        static final WaitingForHello INSTANCE = new WaitingForHello();
        private WaitingForHello() {}
    }

    static final class WaitingForReady extends VoiceGatewayState {
        static final WaitingForReady INSTANCE = new WaitingForReady();
        private WaitingForReady() {}
    }

    static final class WaitingForSessionDescription extends VoiceGatewayState {
        private final int ssrc;

        WaitingForSessionDescription(int ssrc) {
            this.ssrc = ssrc;
        }

        public int getSsrc() {
            return ssrc;
        }
    }

    static final class ReceivingEvents extends VoiceGatewayState {
        private final int ssrc;
        private final byte[] secretKey;
        private final Disposable sending;

        ReceivingEvents(int ssrc, byte[] secretKey, Disposable sending) {
            this.ssrc = ssrc;
            this.secretKey = secretKey;
            this.sending = sending;
        }

        public int getSsrc() {
            return ssrc;
        }

        public byte[] getSecretKey() {
            return secretKey;
        }

        public Disposable getSending() {
            return sending;
        }
    }

    private VoiceGatewayState() {}
}
