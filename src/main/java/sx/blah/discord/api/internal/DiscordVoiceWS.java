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
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.IGuild;
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

	public DiscordVoiceWS(VoiceUpdateResponse response, DiscordClientImpl client) {
		this.client = client;
		this.token = response.token;
		this.endpoint = response.endpoint;
		this.guild = client.getGuildByID(response.guild_id);

		try {
			wsClient = new WebSocketClient(new SslContextFactory());
			wsClient.setDaemon(true);
			wsClient.start();
			System.out.println(response.endpoint);
			wsClient.connect(this, new URI("wss://" + response.endpoint), new ClientUpgradeRequest());
		} catch (Exception e) {
			//TODO: Log exception
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		System.out.println("Voice connected!");

		VoiceIdentifyRequest request = new VoiceIdentifyRequest(guild.getID(), client.getOurUser().getID(), client.ws.sessionId, token);
		send(VoiceOps.IDENTIFY, request);
	}

	@Override
	public void onWebSocketText(String message) {
		JsonObject payload = DiscordUtils.GSON.fromJson(message, JsonObject.class);
		VoiceOps op = VoiceOps.values()[payload.get("op").getAsInt()];
		JsonElement d = payload.has("d") && !(payload.get("d") instanceof JsonNull) ? payload.get("d") : null;

		switch (op) {
			case READY :
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
		}
	}

	private void beginHeartbeat(int interval) {
		keepAlive.scheduleAtFixedRate(() -> {
			send(VoiceOps.HEARTBEAT, System.currentTimeMillis());
		}, 0, interval, TimeUnit.MILLISECONDS);
	}

	public void disconnect(VoiceDisconnectedEvent.Reason reason) {
		// TODO: Other reasons
		switch (reason) {
			case LEFT_CHANNEL:
				client.dispatcher.dispatch(new VoiceDisconnectedEvent(reason));
				client.voiceConnections.remove(guild);
				keepAlive.shutdownNow();
				sendHandler.shutdownNow();
				udpSocket.close();
				getSession().close();
				break;
		}
	}

	private void setupSendThread() {
		Runnable sendThread = new Runnable() {
			char seq = 0;
			int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.

			@Override
			public void run() {
				try {
					byte[] data = guild.getAudioManager().getAudio();
					if (data != null && data.length > 0 && !Discord4J.audioDisabled.get()) {
						AudioPacket packet = new AudioPacket(seq, timestamp, ssrc, data, secret);
						if (!isSpeaking) setSpeaking(true);
						udpSocket.send(packet.asUdpPacket(address));

						if (seq+1 > Character.MAX_VALUE) {
							seq = 0;
						} else {
							seq++;
						}

						timestamp += AudioManager.OPUS_FRAME_SIZE;
					} else if (isSpeaking) {
						setSpeaking(false);
					}
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
			}
		};
		sendHandler.scheduleWithFixedDelay(sendThread, 0, AudioManager.OPUS_FRAME_TIME_AMOUNT-1, TimeUnit.MILLISECONDS);
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
		System.out.println(message);
		if (getSession().isOpen()) {
			getSession().getRemote().sendStringByFuture(message);
		} else {
			System.out.println("Attempt to send message on closed session. This should never happen"); // somehow invalid state?
		}
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		System.out.println("voice error");
		cause.printStackTrace();
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		System.out.println("voice ws disconnected with status code " + statusCode + " and reason " + reason);
	}

}
