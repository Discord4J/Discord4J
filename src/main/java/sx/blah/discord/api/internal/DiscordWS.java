package sx.blah.discord.api.internal;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.IdentifyRequest;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.util.LogMarkers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public class DiscordWS extends WebSocketAdapter {

	private WebSocketClient wsClient;
	private DiscordClientImpl client;
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

	protected long seq = 0;

	private String gateway;

	public DiscordWS(IDiscordClient client, String gateway, boolean isDaemon) {
		this.client = (DiscordClientImpl) client;
		this.gateway = gateway;
		this.dispatchHandler = new DispatchHandler(this, this.client);

		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(isDaemon);
			wsClient.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
			wsClient.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
			wsClient.start();
			wsClient.connect(this, new URI(gateway), new ClientUpgradeRequest());
		} catch (Exception e) {
			//TODO: Log exception
		}
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		GatewayOps op = GatewayOps.values()[payload.get("op").getAsInt()];
		JsonObject d = payload.has("d") && !(payload.get("d") instanceof JsonNull) ? payload.get("d").getAsJsonObject() : null;

		switch (op) {
			case HELLO:
				beginHeartbeat(d.get("heartbeat_interval").getAsInt());
				send(new GatewayPayload(GatewayOps.IDENTIFY, new IdentifyRequest(client.token)));
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
			case INVALID_SESSION:disconnect(DiscordDisconnectedEvent.Reason.INVALID_SESSION_OP); break;
			case HEARTBEAT_ACK: /* TODO: Handle missed pings */ break;

			default:
				Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unhandled opcode received: {} (ignoring), REPORT THIS TO THE DISCORD4J DEV!", op);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		System.out.println("Connected!");
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		System.out.println("closed with statuscode " + statusCode + " and reason " + reason);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		cause.printStackTrace();
	}

	public void send(GatewayOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	public void send(GatewayPayload payload) {
		send(DiscordUtils.GSON.toJson(payload));
	}

	public void send(String message) {
		getSession().getRemote().sendStringByFuture(message);
	}

	protected void beginHeartbeat(long interval) {
		keepAlive.scheduleAtFixedRate(() -> {
			send(GatewayOps.HEARTBEAT, seq);
		}, 0, interval, TimeUnit.MILLISECONDS);
	}

	protected void disconnect(DiscordDisconnectedEvent.Reason reason) {
		// TODO: Handle reconnects
		keepAlive.shutdown();
		getSession().close();
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(payload))));
		String message = reader.lines().collect(Collectors.joining());
		this.onWebSocketText(message);
	}
}
