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
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.voice.VoiceIdentifyRequest;
import sx.blah.discord.api.internal.json.responses.voice.VoiceDescriptionResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceSpeakingResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordVoiceWS extends WebSocketAdapter implements IIDLinkedObject {

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
		this.guild = shard.getGuildByID(Long.parseUnsignedLong(event.guild_id));
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

		send(VoiceOps.IDENTIFY, new VoiceIdentifyRequest(guild.getStringID(), shard.getClient().getOurUser().getStringID(), shard.ws.sessionId, token));
	}

	@Override
	public void onWebSocketText(String message) {
		try {
			JsonNode json = DiscordUtils.MAPPER.readTree(message);
			VoiceOps op = VoiceOps.get(json.get("op").asInt());
			JsonNode d = json.has("d") && !json.get("d").isNull() ? json.get("d") : null;

			switch (op) {
				case READY:
					try {
						VoiceReadyResponse ready = DiscordUtils.MAPPER.treeToValue(d, VoiceReadyResponse.class);
						voiceSocket.setup(endpoint, ready.port, ready.ssrc);
						beginHeartbeat(ready.heartbeat_interval);
					} catch (IOException e) {
						Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error handling voice ready payload: ", e);
					}
					break;
				case SESSION_DESCRIPTION:
					VoiceDescriptionResponse description = DiscordUtils.MAPPER.treeToValue(d, VoiceDescriptionResponse.class);
					voiceSocket.setSecret(description.secret_key);
					voiceSocket.begin();
					break;
				case SPEAKING:
					VoiceSpeakingResponse response = DiscordUtils.MAPPER.treeToValue(d, VoiceSpeakingResponse.class);
					IUser user = getGuild().getUserByID(Long.parseUnsignedLong(response.user_id));
					users.put(response.ssrc, user);
					guild.getClient().getDispatcher().dispatch(new UserSpeakingEvent(user.getVoiceStateForGuild(guild).getChannel(), user, response.ssrc, response.speaking));
					break;
				case UNKNOWN:
					Discord4J.LOGGER.debug(LogMarkers.VOICE_WEBSOCKET, "Received unknown voice opcode, {}", message);
					break;
			}
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "JSON Parsing exception!", e);
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
			shard.voiceWebSockets.remove(guild.getLongID());
			heartbeat.shutdownNow();
			voiceSocket.shutdown();
			if (getSession() != null) getSession().close(1000, null); // Discord doesn't care about the reason
			wsClient.stop();
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error while shutting down voice websocket: ", e);
			}
		}
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket Disconnected.");
	}

	public void send(VoiceOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	private void send(GatewayPayload payload) {
		try {
			send(DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(payload));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "JSON Parsing exception!", e);
		}
	}

	public void send(String message) {
		if (getSession() != null && getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Attempt to send message on closed session: {}", message);
		}
	}

	IGuild getGuild() {
		return this.guild;
	}

	@Override
	public long getLongID() {
		return guild.getLongID();
	}
}
