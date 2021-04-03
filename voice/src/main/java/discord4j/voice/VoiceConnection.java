package discord4j.voice;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.function.Function;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public interface VoiceConnection {

    /**
     * A sequence of {@link VoiceGatewayEvent} received from the Voice Gateway session.
     *
     * @return a {@link Flux} of {@link VoiceGatewayEvent}
     */
    Flux<VoiceGatewayEvent> events();

    /**
     * Return whether this voice connection is currently in the {@link State#CONNECTED} state.
     *
     * @return a {@link Mono} that, upon subscription, returns whether the current state is CONNECTED
     */
    default Mono<Boolean> isConnected() {
        return stateEvents().next().filter(s -> s.equals(State.CONNECTED)).hasElement();
    }

    /**
     * Return a {@link Mono} that completes when this connection reaches a {@link State#CONNECTED} or
     * {@link State#DISCONNECTED} state. Only state transitions made after subscription are taken into account.
     *
     * @return a {@link Mono} that signals one of the CONNECTED or DISCONNECTED states
     */
    default Mono<State> onConnectOrDisconnect() {
        return stateEvents().filter(s -> s.equals(State.CONNECTED) || s.equals(State.DISCONNECTED)).next();
    }

    /**
     * Return a sequence of the {@link State} transitions this voice connection receives.
     *
     * @return a {@link Flux} of {@link State} elements
     */
    Flux<State> stateEvents();

    /**
     * Disconnects this voice connection, tearing down existing resources associated with it.
     *
     * @return a {@link Mono} that, upon subscription, disconnects this voice connection. If an error occurrs, it is
     * emitted through the {@code Mono}
     */
    Mono<Void> disconnect();

    /**
     * Return the guild ID tied to this {@link VoiceConnection}. Unlike {@link #getChannelId()}, this method returns
     * synchronously as voice connections are always bound to a single guild.
     *
     * @return the guild ID of this connection
     */
    Snowflake getGuildId();

    /**
     * Return the current channel ID associated with this {@link VoiceConnection}, if available from caching sources.
     * May return empty if no information is available.
     *
     * @return a {@link Mono} that, upon subscription, returns the channel ID this connection is currently pointing to,
     * if available
     */
    Mono<Snowflake> getChannelId();

    /**
     * Instruct a reconnect procedure on this voice connection.
     *
     * @return a {@link Mono} that, upon subscription, attempts to reconnect to the voice gateway, maintaining the same
     * parameters currently associated to this instance
     */
    Mono<Void> reconnect();

    /**
     * Instruct a reconnect procedure on this voice connection, using a custom {@link Throwable} as cause.
     * Implementations can use this to differentiate between a RESUME action (that does not tear down UDP resources)
     * or a full RECONNECT.
     *
     * @return a {@link Mono} that, upon subscription, attempts to reconnect to the voice gateway, maintaining the same
     * parameters currently associated to this instance
     */
    default Mono<Void> reconnect(Function<ContextView, Throwable> errorCause) {
        return reconnect();
    }

    /**
     * States of a voice connection.
     */
    enum State {
        /**
         * Performing a handshake to establish a voice gateway session.
         */
        CONNECTING,
        /**
         * Voice connection is active and capable of handling audio.
         */
        CONNECTED,
        /**
         * A voice connection that is scheduled for a resume attempt.
         */
        RESUMING,
        /**
         * A voice connection that is scheduled for a reconnect attempt.
         */
        RECONNECTING,
        /**
         * A voice connection that has disconnected.
         */
        DISCONNECTED
    }

}
