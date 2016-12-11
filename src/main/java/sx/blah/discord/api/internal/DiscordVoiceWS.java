package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.GatewayPayload;
import sx.blah.discord.api.internal.json.requests.voice.SelectProtocolRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceIdentifyRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceSpeakingRequest;
import sx.blah.discord.api.internal.json.responses.voice.VoiceDescriptionResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceSpeakingResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.VoiceUserSpeakingEvent;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordVoiceWS extends WebSocketAdapter {

	private WebSocketClient wsClient;

	private DiscordClientImpl client;
	private ShardImpl shard;
	private IGuild guild;
	private int ssrc;
	private DatagramSocket udpSocket;
	private InetSocketAddress address;
	private String endpoint;
	private String token;
	private byte[] secret;
	private boolean isSpeaking = false;

	private ScheduledExecutorService keepAlive = Executors.newSingleThreadScheduledExecutor();
	private ScheduledExecutorService sendHandler = Executors.newSingleThreadScheduledExecutor();

	public DiscordVoiceWS(VoiceUpdateResponse response, ShardImpl shard) {
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
		this.token = response.token;
		this.endpoint = response.endpoint;
		this.guild = client.getGuildByID(response.guild_id);

		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(true);
			wsClient.start();
			wsClient.connect(this, new URI("wss://" + response.endpoint), new ClientUpgradeRequest());
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error while initializing voice websocket: {}", e);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Voice websocket connected.");

		VoiceIdentifyRequest request = new VoiceIdentifyRequest(guild.getID(), client.getOurUser().getID(), shard.ws.sessionId, token);
		send(VoiceOps.IDENTIFY, request);
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		VoiceOps op = VoiceOps.get(payload.get("op").getAsInt());
		JsonElement d = payload.has("d") && !(payload.get("d") instanceof JsonNull) ? payload.get("d") : null;

		switch (op) {
			case READY:
				VoiceReadyResponse ready = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceReadyResponse.class);
				this.ssrc = ready.ssrc;

				try {
					udpSocket = new DatagramSocket();
					address = new InetSocketAddress(endpoint, ready.port);
					Pair<String, Integer> ourIP = doIPDiscovery();

					SelectProtocolRequest request = new SelectProtocolRequest(ourIP.getLeft(), ourIP.getRight());
					send(VoiceOps.SELECT_PAYLOAD, request);

					beginHeartbeat(ready.heartbeat_interval);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SESSION_DESCRIPTION:
				VoiceDescriptionResponse description = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceDescriptionResponse.class);
				this.secret = description.secret_key;

				setupSendThread();
				break;
			case SPEAKING:
				VoiceSpeakingResponse speaking = DiscordUtils.GSON.fromJson(payload.get("d"), VoiceSpeakingResponse.class);
				IUser user = client.getUserByID(speaking.user_id);
				client.dispatcher.dispatch(new VoiceUserSpeakingEvent(user, speaking.ssrc, speaking.isSpeaking));
				break;
			case UNKNOWN:
				Discord4J.LOGGER.debug(LogMarkers.VOICE_WEBSOCKET, "Received unknown voice opcode, {}", message);
				break;
		}
	}

	private void beginHeartbeat(int interval) {
		keepAlive.scheduleAtFixedRate(() -> {
			send(VoiceOps.HEARTBEAT, System.currentTimeMillis());
		}, 0, interval, TimeUnit.MILLISECONDS);
	}

	public void disconnect(VoiceDisconnectedEvent.Reason reason) {
		try {
			client.dispatcher.dispatch(new VoiceDisconnectedEvent(reason));
			client.voiceConnections.remove(guild);
			keepAlive.shutdownNow();
			sendHandler.shutdownNow();
			udpSocket.close();
			if (getSession() != null) getSession().close(1000, null); // Discord doesn't care about the reason
			wsClient.stop();
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error while shutting down voice websocket: ", e);
		}
	}

	private void setupSendThread() {
		Runnable sendThread = new Runnable() {
			char seq = 0;
			int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.

			@Override
			public void run() {
				try {
					if (!udpSocket.isClosed()) {
						byte[] data = guild.getAudioManager().getAudio();
						if (data != null && data.length > 0 && !Discord4J.audioDisabled.get()) {
							AudioPacket packet = new AudioPacket(seq, timestamp, ssrc, data, secret);
							if (!isSpeaking) setSpeaking(true);
							udpSocket.send(packet.asUdpPacket(address));

							if (seq + 1 > Character.MAX_VALUE) {
								seq = 0;
							} else {
								seq++;
							}

							timestamp += AudioManager.OPUS_FRAME_SIZE;
						} else if (isSpeaking) {
							setSpeaking(false);
						}
					}
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
			}
		};
		sendHandler.scheduleWithFixedDelay(sendThread, 0, AudioManager.OPUS_FRAME_TIME_AMOUNT - 1, TimeUnit.MILLISECONDS);
	}

	private void setSpeaking(boolean isSpeaking) {
		this.isSpeaking = isSpeaking;
		send(VoiceOps.SPEAKING, new VoiceSpeakingRequest(isSpeaking));
	}

	private Pair<String, Integer> doIPDiscovery() throws IOException {
		byte[] data = ByteBuffer.allocate(70).putInt(ssrc).array();
		DatagramPacket discoveryPacket = new DatagramPacket(data, data.length, address);
		udpSocket.send(discoveryPacket);

		DatagramPacket responsePacket = new DatagramPacket(new byte[70], 70);
		udpSocket.receive(responsePacket);

		byte[] receivedData = responsePacket.getData();
		String ip = new String(Arrays.copyOfRange(receivedData, 4, 68)).trim();
		int port = ((((int) receivedData[69]) & 0x000000FF) << 8) | (((int) receivedData[68]) & 0x000000FF);

		return Pair.of(ip, port);
	}

	private void send(VoiceOps op, Object payload) {
		send(new GatewayPayload(op, payload));
	}

	private void send(GatewayPayload payload) {
		send(DiscordUtils.GSON_NO_NULLS.toJson(payload));
	}

	private void send(String message) {
		if (getSession() != null && getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Attempt to send message on closed session: {}", message);
		}
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error on voice websocket: ", cause);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket disconnected with status code {} and reason \"{}\".", statusCode, reason);
		disconnect(VoiceDisconnectedEvent.Reason.ABNORMAL_CLOSE);
	}
}
