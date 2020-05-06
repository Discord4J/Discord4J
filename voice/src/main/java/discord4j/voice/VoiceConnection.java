package discord4j.voice;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public interface VoiceConnection {

    Flux<VoiceGatewayEvent> events();

    default Mono<Boolean> isConnected() {
        return stateEvents().next().filter(s -> s.equals(State.CONNECTED)).hasElement();
    }

    Flux<State> stateEvents();

    Mono<Void> disconnect();

    long getGuildId();

    Mono<Void> reconnect();

    enum State {
        CONNECTING, CONNECTED, DISCONNECTED, RECONNECTING
    }

}
