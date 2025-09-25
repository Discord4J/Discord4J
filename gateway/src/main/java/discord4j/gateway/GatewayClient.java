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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.gateway;

import discord4j.common.close.CloseStatus;
import discord4j.discordjson.json.gateway.Dispatch;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.retry.PartialDisconnectException;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.function.Function;

/**
 * Represents a Discord real-time websocket client, called Gateway, implementing its lifecycle.
 * <p>
 * Allows consumers to receive inbound events through {@link #dispatch()} and direct raw payloads through
 * {@link #receiver()} and allows a producer to submit events through {@link #send(Publisher)} and {@link #sender()}.
 * <p>
 * Additionally, supports low-level {@link ByteBuf} based communication through {@link #receiver(Function)} and
 * {@link #sendBuffer(Publisher)}.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway">Gateway</a>
 */
public interface GatewayClient {

    /**
     * Establish a reconnecting gateway connection to the given URL.
     *
     * @param gatewayUrl the URL used to establish a websocket connection
     * @return a {@link Mono} signaling completion of the session. If a non-recoverable error terminates the session,
     * it is emitted as an error through this Mono.
     */
    Mono<Void> execute(String gatewayUrl);

    /**
     * Terminates this client's current gateway connection.
     *
     * @param allowResume if resuming this session after closing is possible. if set to {@code true} the main
     * execution {@link Mono} will complete with a {@link PartialDisconnectException} you can
     * use to perform additional behavior or reconnect.
     * @return a {@link Mono} deferring completion until the disconnection has completed. If this client closed due
     * to an error it is emitted through the Mono. If available, a {@link CloseStatus} will be present.
     */
    Mono<CloseStatus> close(boolean allowResume);

    /**
     * Obtains the {@link Flux} of {@link Dispatch} events inbound from the gateway connection made by this client.
     * <p>
     * Can be used like this, for example, to get all created message events:
     * <pre>
     * gatewayClient.dispatch().ofType(MessageCreate.class)
     *     .subscribe(message -&gt; {
     *         System.out.println("Got a message with content: " + message.getMessage().getContent());
     * });
     * </pre>
     *
     * @return a {@link Flux} of {@link Dispatch} values
     */
    Flux<Dispatch> dispatch();

    /**
     * Obtains the {@link Flux} of raw payloads inbound from the gateway connection made by this client.
     *
     * @return a {@link Flux} of {@link GatewayPayload} values
     */
    Flux<GatewayPayload<?>> receiver();

    /**
     * Obtains a {@link Flux} of raw payloads inbound from the gateway connection made by this client, transformed by a
     * mapping function.
     *
     * @param mapper a mapping function turning raw {@link ByteBuf} objects into a given type
     * @param <T> the type of the resulting inbound {@link Flux}
     * @return a {@link Flux} of raw payloads transformed by a mapping function
     */
    <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper);

    /**
     * Retrieves a new {@link reactor.core.publisher.Sinks.Many} to safely produce outbound values using
     * {@link reactor.core.publisher.Sinks.Many#tryEmitNext(Object)} or {@link reactor.core.publisher.Sinks.Many#emitNext(Object, reactor.core.publisher.Sinks.EmitFailureHandler)}.
     *
     * @return a serializing {@link reactor.core.publisher.Sinks.Many}
     */
    Sinks.Many<GatewayPayload<?>> sender();

    /**
     * Sends a sequence of {@link GatewayPayload payloads} through this {@link GatewayClient} and returns a
     * {@link Mono} that signals completion when the payloads have been sent.
     *
     * @param publisher a sequence of outbound payloads
     * @return a {@link Mono} completing when payloads have been sent
     */
    default Mono<Void> send(Publisher<? extends GatewayPayload<?>> publisher) {
        return Flux.from(publisher)
                .doOnNext(payload -> sender().emitNext(payload, Sinks.EmitFailureHandler.FAIL_FAST))
                .then();
    }

    /**
     * Sends a sequence of {@link ByteBuf} payloads through this {@link GatewayClient} and returns a {@link Mono}
     * that signals completion when the given publisher completes.
     * <p>
     * Sequences produced this way are not expected to be validated against errors or invalid input by the underlying
     * implementation.
     *
     * @param publisher a sequence of outbound payloads
     * @return a {@link Mono} signaling completion, if an error occurs while producing it is emitted through the Mono
     */
    Mono<Void> sendBuffer(Publisher<ByteBuf> publisher);

    /**
     * Return number of shards this client operates under.
     *
     * @return a positive integer representing the number of shards
     */
    int getShardCount();

    /**
     * Retrieve the ID of the current gateway session.
     *
     * @return the ID of the current gateway session. Used for resuming and voice.
     */
    String getSessionId();

    /**
     * Gets the current heartbeat sequence.
     *
     * @return an integer representing the current gateway sequence
     */
    int getSequence();

    /**
     * Return a sequence of the {@link GatewayConnection.State} transitions this client receives.
     *
     * @return a {@link Flux} of state elements
     */
    Flux<GatewayConnection.State> stateEvents();

    /**
     * Returns whether this GatewayClient is currently connected to Discord Gateway therefore capable to send and
     * receive payloads.
     *
     * @return a {@link Mono} that upon subscription, returns true if the gateway connection is currently
     * established, false otherwise.
     */
    Mono<Boolean> isConnected();

    /**
     * Gets the amount of time it last took Discord to respond to a heartbeat with an ack.
     *
     * @return the duration which Discord took to respond to the last heartbeat with an ack.
     */
    Duration getResponseTime();
}
