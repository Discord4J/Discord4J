/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.IdentifyRequest;
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.requests.ResumeRequest;
import sx.blah.discord.api.internal.json.responses.ReadyResponse;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.util.LogMarkers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

/**
 * Facilitates a websocket connection between the client and Discord's Gateway.
 */
public class DiscordWS extends WebSocketAdapter {

	/**
	 * The version of the Discord Gateway that is used by Discord4J.
	 */
	public static final String GATEWAY_VERSION = "6";

	/**
	 * The current state of the connection.
	 */
	State state;
	/**
	 * The websocket client used to communicate with the gateway.
	 */
	private WebSocketClient wsClient;

	/**
	 * The client associated with the gateway connection.
	 */
	DiscordClientImpl client;
	/**
	 * The shard associated with the gateway connection.
	 */
	ShardImpl shard;
	/**
	 * The websocket host URL.
	 */
	private String gateway;

	/**
	 * The last sequence number received in an event from Discord.
	 */
	long seq = 0;
	/**
	 * The ID of the current gateway session. Used for resuming.
	 */
	String sessionId;

	/**
	 * The handler for OP 0 event dispatches from Discord.
	 */
	private DispatchHandler dispatchHandler;
	/**
	 * The handler for heartbeat information.
	 */
	HeartbeatHandler heartbeatHandler;

	/**
	 * The presence object that should be sent to Discord when identifying.
	 */
	private final PresenceUpdateRequest identifyPresence;

	/**
	 * Indicates whether the bot has received all available guilds.
	 */
	public boolean isReady = false;

	/**
	 * Indicates whether the bot has received the initial Ready payload from Discord.
	 */
	public boolean hasReceivedReady = false;

	DiscordWS(IShard shard, String gateway, int maxMissedPings, PresenceUpdateRequest identifyPresence) {
		this.client = (DiscordClientImpl) shard.getClient();
		this.shard = (ShardImpl) shard;
		this.gateway = gateway;
		this.dispatchHandler = new DispatchHandler(this, this.shard);
		this.heartbeatHandler = new HeartbeatHandler(this, maxMissedPings);
		this.identifyPresence = identifyPresence;
		this.state = State.CONNECTING;
	}

	@Override
	public void onWebSocketText(String message) {
		try {
			if (Discord4J.LOGGER.isTraceEnabled(LogMarkers.WEBSOCKET_TRAFFIC)) {
				Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET_TRAFFIC, "Received: " + message);
			}

			JsonNode json = DiscordUtils.MAPPER.readTree(message);
			GatewayOps op = GatewayOps.get(json.get("op").asInt());
			JsonNode d = json.has("d") && !json.get("d").isNull() ? json.get("d") : null;

			if (json.has("s") && !json.get("s").isNull()) seq = json.get("s").longValue();

			switch (op) {
				case HELLO:
					Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Shard {} _trace: {}", shard.getInfo()[0], d.get("_trace").toString());

					heartbeatHandler.begin(d.get("heartbeat_interval").intValue());
					if (this.state != State.RESUMING) {
						send(GatewayOps.IDENTIFY, new IdentifyRequest(client.getToken(), shard.getInfo(), identifyPresence));
					} else {
						client.reconnectManager.onReconnectSuccess();
						send(GatewayOps.RESUME, new ResumeRequest(client.getToken(), sessionId, seq));
					}
					break;
				case RECONNECT:
					this.state = State.RESUMING;
					client.getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.RECONNECT_OP, shard));
					heartbeatHandler.shutdown();
					send(GatewayOps.RESUME, new ResumeRequest(client.getToken(), sessionId, seq));
					break;
				case DISPATCH:
					try {
						dispatchHandler.handle(json);
					} catch (Exception e) {
						Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
					}
					break;
				case INVALID_SESSION:
					this.state = State.RECONNECTING;
					client.getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.INVALID_SESSION_OP, shard));
					invalidate();
					send(GatewayOps.IDENTIFY, new IdentifyRequest(client.getToken(), shard.getInfo(), null)); // TODO: try to maintain previous presence?
					break;
				case HEARTBEAT:
					send(GatewayOps.HEARTBEAT, seq);
				case HEARTBEAT_ACK:
					heartbeatHandler.ack();
					break;
				case UNKNOWN:
					Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Received unknown opcode, {}", message);
					break;
			}
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "JSON Parsing exception!", e);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Websocket Connected.");
		super.onWebSocketConnect(sess);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Shard {} websocket disconnected with status code {} and reason \"{}\".", shard.getInfo()[0], statusCode, reason);

		isReady = false;
		hasReceivedReady = false;
		heartbeatHandler.shutdown();

		if (!(this.state == State.DISCONNECTING || statusCode == 4003 || statusCode == 4004 || statusCode == 4005 || statusCode == 4010)
				&& !(statusCode == 1001 && reason != null && reason.equals("Shutdown"))) {
			this.state = State.RESUMING;
			client.getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.ABNORMAL_CLOSE, shard));
			client.reconnectManager.scheduleReconnect(this);
		}
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);

		if (cause instanceof UnresolvedAddressException) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Caught UnresolvedAddressException. Internet outage?");
		} else if (cause instanceof UnknownHostException) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Caught UnknownHostException. Internet outage?");
		} else if (cause instanceof UpgradeException) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Caught UpgradeException. Internet outage?");
		} else if (cause instanceof ClosedChannelException) {
			Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Discord rejected our connection, reconnecting...");
		} else {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered websocket error: ", cause);
		}

		if (this.state == State.RESUMING) {
			client.reconnectManager.onReconnectError();
		}
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(payload, offset, len))));
		onWebSocketText(reader.lines().collect(Collectors.joining()));
		try {
			reader.close();
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered websocket error: ", e);
		}
	}

	/**
	 * Opens the initial websocket connection with the gateway.
	 * If a connection was already open (in the case of reconnecting), it will be asynchronously closed.
	 */
	void connect() {
		WebSocketClient previous = wsClient; // for cleanup
		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
			wsClient.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
			wsClient.start();
			wsClient.connect(this, new URI(gateway), new ClientUpgradeRequest());
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered error while connecting websocket: ", e);
		} finally {
			if (previous != null) {
				CompletableFuture.runAsync(() -> {
					try {
						previous.stop();
					} catch (Exception e) {
						Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error while stopping previous websocket: ", e);
					}
				});
			}
		}
	}

	/**
	 * Closes the websocket connection and resets state.
	 */
	void shutdown() {
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Shard {} shutting down.", shard.getInfo()[0]);
		this.state = State.DISCONNECTING;

		try {
			heartbeatHandler.shutdown();
			getSession().close(1000, null); // Discord doesn't care about the reason
			wsClient.stop();
			hasReceivedReady = false;
			isReady = false;
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error while shutting down websocket: ", e);
		}
	}

	/**
	 * Invalidates all information of this connection and associated shard.
	 */
	private void invalidate() {
		this.isReady = false;
		this.hasReceivedReady = false;
		this.seq = 0;
		this.sessionId = null;
		this.shard.guildCache.clear();
		this.shard.privateChannels.clear();
	}

	/**
	 * Sends a message on the websocket.
	 *
	 * @param op The opcode for the message.
	 * @param payload The data payload to use for {@link GatewayPayload#d}.
	 */
	public void send(GatewayOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	/**
	 * Sends a message on the websocket.
	 *
	 * @param payload The message to serialize and send.
	 */
	public void send(GatewayPayload payload) {
		try {
			send(DiscordUtils.MAPPER.writeValueAsString(payload));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "JSON Parsing exception!", e);
		}
	}

	/**
	 * Sends a message on the websocket.
	 *
	 * @param message The message to send.
	 */
	public void send(String message) {
		String filteredMessage = message.replace(client.getToken(), "hunter2");

		if (getSession() != null && getSession().isOpen()) {
			Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET_TRAFFIC, "Sending: " + filteredMessage);
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Attempt to send message on closed session: {}", filteredMessage);
		}
	}

	/**
	 * Represents the state of the websocket connection.
	 */
	enum State {
		/**
		 * Indicates that the websocket isn't connected and is idle.
		 */
		IDLE,

		/**
		 * Indicates that the websocket is attempting to connect.
		 */
		CONNECTING,

		/**
		 * Indicates that the Ready payload was received from Discord.
		 * @see DispatchHandler#ready(ReadyResponse).
		 */
		READY,

		/**
		 * Indicates that the websocket is attempting to reconnect to the gateway.
		 * This is set on {@link GatewayOps#INVALID_SESSION} and when the heartbeat handler detects a zombie connection.
		 */
		RECONNECTING,

		/**
		 * Indicates that the websocket is attempting to resume on the gateway.
		 * This is set on {@link GatewayOps#RECONNECT} and when the underlying websocket connection is abruptly closed.
		 */
		RESUMING,

		/**
		 * Indicates that the websocket is intentionally disconnecting.
		 * This is set by {@link #shutdown()}.
		 */
		DISCONNECTING
	}
}
