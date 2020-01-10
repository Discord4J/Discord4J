package discord4j.voice;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public interface VoiceConnection {

    Flux<VoiceGatewayEvent> events();

    boolean isConnected();

    State getState();

    Mono<Void> disconnect();

    enum State {
        CONNECTING, CONNECTED, DISCONNECTED, RECONNECTING
    }

}
