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

import discord4j.common.jackson.Possible;
import discord4j.gateway.json.*;
import discord4j.gateway.json.dispatch.Dispatch;
import discord4j.gateway.json.dispatch.Ready;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static discord4j.common.LogUtil.format;

/**
 * Registry for operating on gateway {@link PayloadData} objects, handling each lifecycle {@link Opcode}.
 */
public abstract class PayloadHandlers {

    private static final Map<Opcode<?>, PayloadHandler<?>> handlerMap = new HashMap<>();

    static {
        addHandler(Opcode.DISPATCH, PayloadHandlers::handleDispatch);
        addHandler(Opcode.HEARTBEAT, PayloadHandlers::handleHeartbeat);
        addHandler(Opcode.RECONNECT, PayloadHandlers::handleReconnect);
        addHandler(Opcode.INVALID_SESSION, PayloadHandlers::handleInvalidSession);
        addHandler(Opcode.HELLO, PayloadHandlers::handleHello);
        addHandler(Opcode.HEARTBEAT_ACK, PayloadHandlers::handleHeartbeatAck);
    }

    private static <T extends PayloadData> void addHandler(Opcode<T> op, PayloadHandler<T> handler) {
        handlerMap.put(op, handler);
    }

    /**
     * Process a {@link discord4j.gateway.json.PayloadData} object together with its context, reacting to it.
     *
     * @param context the PayloadContext used with this PayloadData object
     * @param <T> the PayloadData type
     */
    @SuppressWarnings("unchecked")
    public static <T extends PayloadData> void handle(PayloadContext<T> context) {
        PayloadHandler<T> entry = (PayloadHandler<T>) handlerMap.get(context.getPayload().getOp());
        if (entry != null) {
            entry.handle(context);
        }
    }

    private static void handleDispatch(PayloadContext<Dispatch> context) {
        if (context.getData() instanceof Ready) {
            String newSessionId = ((Ready) context.getData()).getSessionId();
            context.getClient().sessionId().set(newSessionId);
        }
        if (context.getData() != null) {
            context.getClient().dispatchSink().next(context.getData());
        }
    }

    private static void handleHeartbeat(PayloadContext<Heartbeat> context) {
        log.debug(format(context.getContext(), "Received heartbeat"));
        context.getClient().sender().next(GatewayPayload.heartbeat(new Heartbeat(context.getClient().sequence().get())));
    }

    private static void handleReconnect(PayloadContext<?> context) {
        context.getHandler().error(new RuntimeException("Reconnecting due to reconnect packet received"));
    }

    private static void handleInvalidSession(PayloadContext<InvalidSession> context) {
        DefaultGatewayClient client = context.getClient();
        if (context.getData().isResumable()) {
            String token = client.token();
            client.sender().next(GatewayPayload.resume(
                    new Resume(token, client.getSessionId(), client.sequence().get())));
        } else {
            client.resumable().set(false);
            context.getHandler().error(new RuntimeException("Reconnecting due to non-resumable session invalidation"));
        }
    }

    private static void handleHello(PayloadContext<Hello> context) {
        Duration interval = Duration.ofMillis(context.getData().getHeartbeatInterval());
        DefaultGatewayClient client = context.getClient();
        client.heartbeat().start(interval);

        if (client.resumable().get()) {
            log.info(format(context.getContext(), "Attempting to RESUME from {}"), client.sequence().get());
            client.sender().next(GatewayPayload.resume(
                    new Resume(client.token(), client.getSessionId(), client.sequence().get())));
        } else {
            IdentifyProperties props = new IdentifyProperties(System.getProperty("os.name"), "Discord4J", "Discord4J");
            IdentifyOptions options = client.identifyOptions();
            int[] shard = new int[]{options.getShardIndex(), options.getShardCount()};
            Identify identify = new Identify(client.token(), props, false, 250,
                    Optional.of(shard).map(Possible::of).orElse(Possible.absent()),
                    Optional.ofNullable(options.getInitialStatus()).map(Possible::of).orElse(Possible.absent()));
            client.sender().next(GatewayPayload.identify(identify));
        }
    }

    private static void handleHeartbeatAck(PayloadContext<?> context) {
        context.getClient().ackHeartbeat();
        log.debug(format(context.getContext(), "Heartbeat acknowledged after {}"), context.getClient().getResponseTimeDuration());
    }

    private static final Logger log = Loggers.getLogger("discord4j.gateway.handler");

}
