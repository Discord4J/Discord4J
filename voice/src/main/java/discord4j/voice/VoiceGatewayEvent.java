package discord4j.voice;

public interface VoiceGatewayEvent {

    class Stop implements VoiceGatewayEvent {}
    class Start implements VoiceGatewayEvent {

        final String gatewayUrl;
        final Runnable connectedCallback;

        Start(String gatewayUrl, Runnable connectedCallback) {
            this.gatewayUrl = gatewayUrl;
            this.connectedCallback = connectedCallback;
        }
    }
}
