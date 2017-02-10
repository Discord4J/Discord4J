package sx.blah.discord.api.internal;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.voice.SelectProtocolRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceIdentifyRequest;
import sx.blah.discord.api.internal.json.responses.voice.VoiceDescriptionResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceSpeakingResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

public class DiscordVoiceWS extends WebSocketAdapter {

	private WebSocketClient wsClient;
	private ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Discord Voice WS Heartbeat"));
	private UDPVoiceSocket voiceSocket = new UDPVoiceSocket(this);

	private final ShardImpl shard;
	private final String endpoint;
	private final String token;
	private final IGuild guild;

	final Map<Integer, IUser> users = new ConcurrentHashMap<>();

	DiscordVoiceWS(IShard shard, VoiceUpdateResponse event) {
		this.shard = (ShardImpl) shard;
		this.endpoint = event.endpoint.replace(":80", "");
		this.token = event.token;
		this.guild = shard.getGuildByID(event.guild_id);
	}

	void connect() {
		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(true);
			wsClient.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
			wsClient.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
			wsClient.start();
			wsClient.connect(this, new URI("wss://" + endpoint), new ClientUpgradeRequest());
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error while connecting voice websocket: ", e);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket Connected.");

		send(VoiceOps.IDENTIFY, new VoiceIdentifyRequest(guild.getID(), shard.getClient().getOurUser().getID(), shard.ws.sessionId, token));
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		VoiceOps op = VoiceOps.get(payload.get("op").getAsInt());

		switch (op) {
			case READY:
				try {
					VoiceReadyResponse ready = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceReadyResponse.class);
					voiceSocket.setup(endpoint, ready.port, ready.ssrc);
					beginHeartbeat(ready.heartbeat_interval);
				} catch (IOException e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error handling voice ready payload: ", e);
				}
				break;
			case SESSION_DESCRIPTION:
				VoiceDescriptionResponse description = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceDescriptionResponse.class);
				voiceSocket.setSecret(description.secret_key);
				voiceSocket.begin();

				break;
			case SPEAKING:
				VoiceSpeakingResponse response = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceSpeakingResponse.class);
				IUser user = getGuild().getUserByID(response.user_id);
				users.put(response.ssrc, user);
				guild.getClient().getDispatcher().dispatch(new UserSpeakingEvent(user.getVoiceStateForGuild(guild).getChannel(), user, response.ssrc, response.isSpeaking));
				break;
		}
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket disconnected with status code {} and reason \"{}\"", statusCode, reason);
		disconnect(VoiceDisconnectedEvent.Reason.ABNORMAL_CLOSE); // TODO: Reconnect?
	}

	private void beginHeartbeat(int interval) {
		heartbeat.scheduleAtFixedRate(() -> send(VoiceOps.HEARTBEAT, System.currentTimeMillis()), 0, interval, TimeUnit.MILLISECONDS);
	}

	public void disconnect(VoiceDisconnectedEvent.Reason reason) {
		try {
			shard.getClient().getDispatcher().dispatch(new VoiceDisconnectedEvent(getGuild(), reason));
			shard.voiceWebSockets.remove(guild);
			heartbeat.shutdownNow();
			voiceSocket.shutdown();
			if (getSession() != null) getSession().close(1000, null); // Discord doesn't care about the reason
			wsClient.stop();
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error while shutting down voice websocket: ", e);
			}
		}
	}

	public void send(VoiceOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	public void send(GatewayPayload payload) {
		send(DiscordUtils.GSON.toJson(payload));
	}

	public void send(String message) {
		if (getSession() != null && getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Attempt to send message on closed session: {}", message);
		}
	}

	IGuild getGuild() {
		return this.guild;
	}
}
