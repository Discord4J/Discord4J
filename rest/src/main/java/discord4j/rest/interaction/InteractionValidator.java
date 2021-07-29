package discord4j.rest.interaction;

import reactor.core.publisher.Mono;

/**
 * Implement to validate interactions
 */
public interface InteractionValidator {
    /**
     * See https://discord.com/developers/docs/interactions/slash-commands#security-and-authorization
     *
     * @param signature the signature header of the request
     * @param timestamp the timestamp header of the request
     * @param body      the body of the request
     * @return true if the request is valid, false otherwise
     */
    Mono<Boolean> validateSignature(String signature, String timestamp, String body);
}
