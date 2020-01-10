package discord4j.voice;

import reactor.core.publisher.MonoSink;

public interface VoiceGatewayEvent {

    class Stop implements VoiceGatewayEvent {}
    class Start implements VoiceGatewayEvent {

        final String gatewayUrl;
        final MonoSink<VoiceConnection> connectedCallback;

        Start(String gatewayUrl, MonoSink<VoiceConnection> connectedCallback) {
            this.gatewayUrl = gatewayUrl;
            this.connectedCallback = connectedCallback;
        }
    }
}
