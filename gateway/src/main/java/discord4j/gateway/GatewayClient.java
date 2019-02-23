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

import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.dispatch.Dispatch;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * Represents a Discord WebSocket client, called Gateway, implementing its lifecycle.
 * <p>
 * Allows consumers to receive inbound events through {@link #dispatch()} and direct raw payloads through
 * {@link #receiver()} and allows a producer to submit events through {@link #sender()}.
 */
public interface GatewayClient {

    /**
     * Establish a reconnecting gateway connection to the given URL.
     *
     * @param gatewayUrl the URL used to establish a websocket connection
     * @return a Mono signaling completion
     */
    Mono<Void> execute(String gatewayUrl);

    /**
     * Establish a reconnecting gateway connection to the given URL, allowing an ad-hoc observer to be notified.
     *
     * @param gatewayUrl the URL used to establish a websocket connection
     * @param additionalObserver an additional observer to be notified of events
     * @return a Mono signaling completion
     */
    Mono<Void> execute(String gatewayUrl, GatewayObserver additionalObserver);

    /**
     * Terminates this client's current gateway connection, and optionally, reconnect to it.
     *
     * @param reconnect if this client should attempt to reconnect after closing
     * @return a {@link Mono} deferring completion until the disconnection is completed, or if reconnecting, an empty
     * one.
     */
    Mono<Void> close(boolean reconnect);

    /**
     * Obtains the Flux of Dispatch events inbound from the gateway connection made by this client.
     * <p>
     * Can be used like this, for example, to get all created message events:
     * <pre>
     * gatewayClient.dispatch().ofType(MessageCreate.class)
     *     .subscribe(message -&gt; {
     *         System.out.println("Got a message with content: " + message.getMessage().getContent());
     * });
     * </pre>
     *
     * @return a Flux of Dispatch values
     */
    Flux<Dispatch> dispatch();

    /**
     * Obtains the Flux of raw payloads inbound from the gateway connection made by this client.
     *
     * @return a Flux of GatewayPayload values
     */
    Flux<GatewayPayload<?>> receiver();

    /**
     * Retrieves a new FluxSink to safely produce outbound values. By Reactive Streams Specs Rule 2.12 this can't be
     * called twice from the same instance (based on object equality).
     *
     * @return a serializing FluxSink
     */
    FluxSink<GatewayPayload<?>> sender();

    /**
     * Sends a sequence of {@link GatewayPayload payloads} through this {@link GatewayClient} and returns a
     * {@link Mono} that signals completion when the payloads have been sent.
     *
     * @param publisher a sequence of outbound payloads
     * @return a {@link Mono} completing when payloads have been sent
     */
    default Mono<Void> send(Publisher<GatewayPayload<?>> publisher) {
        return Flux.from(publisher)
                .doOnNext(payload -> sender().next(payload))
                .then();
    }

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
     * Returns whether this GatewayClient is currently connected to Discord Gateway therefore capable to send and
     * receive payloads.
     *
     * @return true if the gateway connection is currently established, false otherwise.
     */
    boolean isConnected();

    /**
     * Gets the amount of time it last took Discord to respond to a heartbeat with an ack.
     *
     * @return the time in milliseconds took Discord to respond to the last heartbeat with an ack.
     */
    long getResponseTime();
}
