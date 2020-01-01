package discord4j.voice;

import reactor.core.publisher.Mono;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public interface VoiceConnection {

    Mono<Void> disconnect();

}
