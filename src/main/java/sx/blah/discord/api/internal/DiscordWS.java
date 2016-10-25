package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.IdentifyRequest;
import sx.blah.discord.api.internal.json.requests.ResumeRequest;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.LoginEvent;
import sx.blah.discord.util.LogMarkers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public class DiscordWS extends WebSocketAdapter {

	private WebSocketClient wsClient;

	private DiscordClientImpl client;
	private ShardImpl shard;
	private String gateway;

	private DispatchHandler dispatchHandler;
	private ScheduledExecutorService keepAlive = Executors.newSingleThreadScheduledExecutor();

	private long seq = 0;
	protected String sessionId;

	/**
	 * When the bot has received all available guilds.
	 */
	public boolean isReady = false;

	/**
	 * When the bot has received the initial Ready payload from Discord.
	 */
	public boolean hasReceivedReady = false;

	public DiscordWS(IShard shard, String gateway, boolean isDaemon) {
		this.client = (DiscordClientImpl) shard.getClient();
		this.shard = (ShardImpl) shard;
		this.gateway = gateway;
		this.dispatchHandler = new DispatchHandler(this, this.shard);

		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(isDaemon);
			wsClient.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
			wsClient.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
			wsClient.start();
			wsClient.connect(this, new URI(gateway), new ClientUpgradeRequest());
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered error while initializing websocket: {}", e);
		}
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		GatewayOps op = GatewayOps.values()[payload.get("op").getAsInt()];
		JsonElement d = payload.has("d") && !payload.get("d").isJsonNull() ? payload.get("d") : null;

		if (payload.has("s") && !payload.get("s").isJsonNull()) seq = payload.get("s").getAsLong();

		switch (op) {
			case HELLO:
				beginHeartbeat(d.getAsJsonObject().get("heartbeat_interval").getAsInt());
				send(new GatewayPayload(GatewayOps.IDENTIFY, new IdentifyRequest(client.token, shard.getInfo())));
				break;
			case RECONNECT:
				try {
					disconnect(DiscordDisconnectedEvent.Reason.RECONNECT_OP);
					wsClient.connect(this, new URI(this.gateway), new ClientUpgradeRequest());
				} catch (IOException | URISyntaxException e) {
					Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Encountered error while handling RECONNECT op, REPORT THIS TO THE DISCORD4J DEV! {}", e);
				}
				break;
			case DISPATCH: dispatchHandler.handle(payload); break;
			case INVALID_SESSION: disconnect(DiscordDisconnectedEvent.Reason.INVALID_SESSION_OP); break;
			case HEARTBEAT_ACK: /* TODO: Handle missed pings */ break;

			default:
				Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unhandled opcode received: {} (ignoring), REPORT THIS TO THE DISCORD4J DEV!", op);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Websocket Connected.");
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Websocket disconnected with status code {} and reason \"{}\".", statusCode, reason);

		if (statusCode == 1006 || statusCode == 1001) { // Which status codes represent errors? All but 1000?
			disconnect(DiscordDisconnectedEvent.Reason.ABNORMAL_CLOSE);
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
	}

	public void send(GatewayOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	public void send(GatewayPayload payload) {
		send(DiscordUtils.GSON.toJson(payload));
	}

	public void send(String message) {
		if (getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Attempt to send message on closed session.");
		}
	}

	protected void beginHeartbeat(long interval) {
		if (keepAlive.isShutdown()) keepAlive = Executors.newSingleThreadScheduledExecutor();

		keepAlive.scheduleAtFixedRate(() -> {
			send(GatewayOps.HEARTBEAT, seq);
		}, 0, interval, TimeUnit.MILLISECONDS);
	}

	protected void disconnect(DiscordDisconnectedEvent.Reason reason) {
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Disconnected with reason {}", reason);

		keepAlive.shutdown();
		switch (reason) {
			case INVALID_SESSION_OP:
			case ABNORMAL_CLOSE:
				Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "WS should reconnect panda is bad.");
				getSession().close(); // TODO: Definitely not
				break;
			case LOGGED_OUT:
				getSession().close();
				break;
			default:
				Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unhandled disconnect reason");
		}
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(payload))));
		String message = reader.lines().collect(Collectors.joining());
		this.onWebSocketText(message);
	}
}
