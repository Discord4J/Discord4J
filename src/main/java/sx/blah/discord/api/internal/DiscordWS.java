package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.IdentifyRequest;
import sx.blah.discord.api.internal.json.requests.ResumeRequest;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.util.LogMarkers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public class DiscordWS extends WebSocketAdapter {

	State state;
	private WebSocketClient wsClient;

	private DiscordClientImpl client;
	ShardImpl shard;
	private String gateway;

	private long seq = 0;
	String sessionId;

	private DispatchHandler dispatchHandler;
	private ScheduledExecutorService keepAlive = Executors.newSingleThreadScheduledExecutor();

	/**
	 * When the bot has received all available guilds.
	 */
	public boolean isReady = false;

	/**
	 * When the bot has received the initial Ready payload from Discord.
	 */
	public boolean hasReceivedReady = false;

	DiscordWS(IShard shard, String gateway) {
		this.client = (DiscordClientImpl) shard.getClient();
		this.shard = (ShardImpl) shard;
		this.gateway = gateway;
		this.dispatchHandler = new DispatchHandler(this, this.shard);
		this.state = State.CONNECTING;
		connect();
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		GatewayOps op = GatewayOps.get(payload.get("op").getAsInt());
		JsonElement d = payload.has("d") && !payload.get("d").isJsonNull() ? payload.get("d") : null;

		if (payload.has("s") && !payload.get("s").isJsonNull()) seq = payload.get("s").getAsLong();

		switch (op) {
			case HELLO:
				Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Shard {} _trace: {}", shard.getInfo()[0], d.getAsJsonObject().get("_trace"));

				beginHeartbeat(d.getAsJsonObject().get("heartbeat_interval").getAsInt());
				if (this.state != State.RESUMING) {
					send(GatewayOps.IDENTIFY, new IdentifyRequest(client.getToken(), shard.getInfo()));
				} else {
					client.reconnectManager.onReconnectSuccess();
					send(GatewayOps.RESUME, new ResumeRequest(client.getToken(), sessionId, seq));
				}
				break;
			case RECONNECT:
				this.state = State.RESUMING;
				client.getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.RECONNECT_OP, shard));
				keepAlive.shutdown();
				send(GatewayOps.RESUME, new ResumeRequest(client.getToken(), sessionId, seq));
				break;
			case DISPATCH: dispatchHandler.handle(payload); break;
			case INVALID_SESSION:
				this.state = State.RECONNECTING;
				client.getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.INVALID_SESSION_OP, shard));
				invalidate();
				send(GatewayOps.IDENTIFY, new IdentifyRequest(client.getToken(), shard.getInfo()));
				break;
			case HEARTBEAT: send(GatewayOps.HEARTBEAT, seq);
			case HEARTBEAT_ACK: /* TODO: Handle missed pings */ break;
			case UNKNOWN:
				Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Received unknown opcode, {}", message);
				break;
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Websocket Connected.");
		super.onWebSocketConnect(sess);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Shard {} websocket disconnected with status code {} and reason \"{}\".", shard.getInfo()[0], statusCode, reason);

		keepAlive.shutdown();
		if (this.state != State.DISCONNECTING && statusCode != 4003 && statusCode != 4004 && statusCode != 4005 && statusCode != 4010) {
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
		} else {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered websocket error: {}", cause);
		}

		if (this.state == State.RESUMING) {
			client.reconnectManager.onReconnectError();
		}
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(payload))));
		String message = reader.lines().collect(Collectors.joining());
		this.onWebSocketText(message);
	}

	void connect() {
		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(true);
			wsClient.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
			wsClient.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
			wsClient.start();
			wsClient.connect(this, new URI(gateway), new ClientUpgradeRequest());
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered error while connecting websocket: {}", e);
		}
	}

	void shutdown() {
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Shard {} shutting down.", shard.getInfo()[0]);
		try {
			keepAlive.shutdown();
			getSession().close(1000, null); // Discord doesn't care about the reason
			wsClient.stop();
			this.state = State.IDLE;
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error while shutting down websocket: {}", e);
		}
	}

	private void beginHeartbeat(long interval) {
		if (keepAlive.isShutdown()) keepAlive = Executors.newSingleThreadScheduledExecutor();

		keepAlive.scheduleAtFixedRate(() -> {
			Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Sending heartbeat on shard {}", shard.getInfo()[0]);
			send(GatewayOps.HEARTBEAT, seq);
		}, 0, interval, TimeUnit.MILLISECONDS);
	}

	private void invalidate() {
		this.seq = 0;
		this.sessionId = null;
		this.shard.guildList.clear();
		this.shard.privateChannels.clear();
	}

	public void send(GatewayOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	public void send(GatewayPayload payload) {
		send(DiscordUtils.GSON.toJson(payload));
	}

	public void send(String message) {
		if (getSession() != null && getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Attempt to send message on closed session.");
		}
	}

	enum State {
		IDLE,
		CONNECTING,
		READY,
		RECONNECTING,
		RESUMING,
		DISCONNECTING
	}
}
