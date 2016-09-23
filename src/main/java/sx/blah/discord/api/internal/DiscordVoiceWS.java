package sx.blah.discord.api.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.VoicePingEvent;
import sx.blah.discord.handle.impl.events.VoiceUserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.api.internal.json.requests.VoiceConnectRequest;
import sx.blah.discord.api.internal.json.requests.VoiceKeepAliveRequest;
import sx.blah.discord.api.internal.json.requests.VoiceSpeakingRequest;
import sx.blah.discord.api.internal.json.requests.VoiceUDPConnectRequest;
import sx.blah.discord.api.internal.json.responses.VoiceUpdateResponse;
import sx.blah.discord.util.LogMarkers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterInputStream;

import static sx.blah.discord.Discord4J.LOGGER;

@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE, maxIdleTime = Integer.MAX_VALUE, maxTextMessageSize = Integer.MAX_VALUE)
public class DiscordVoiceWS {

	public static final int OP_INITIAL_CONNECTION = 2;
	public static final int OP_HEARTBEAT_RETURN = 3;
	public static final int OP_CONNECTING_COMPLETED = 4;
	public static final int OP_USER_SPEAKING_UPDATE = 5;

	public AtomicBoolean isConnected = new AtomicBoolean(true);
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		private volatile int executorCount = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("Discord4J Voice WebSocket Client Executor "+(executorCount++));
			return thread;
		}
	});

	private DiscordClientImpl client;

	private IGuild guild;

	private int ssrc;
	private VoiceUpdateResponse event;
	private DatagramSocket udpSocket;

	private InetSocketAddress addressPort;
	private boolean isSpeaking;

	private byte[] secret;

	private Session session;
	private final WebSocketClient wsClient;

	public static DiscordVoiceWS connect(VoiceUpdateResponse response, IDiscordClient client) throws Exception {
		SslContextFactory sslFactory = new SslContextFactory();
		WebSocketClient wsClient = new WebSocketClient(sslFactory);
		wsClient.setDaemon(true);
		DiscordVoiceWS socket = new DiscordVoiceWS(response, (DiscordClientImpl) client, wsClient);
		wsClient.start();
		ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
		upgradeRequest.setHeader("Accept-Encoding", "gzip, deflate");
		wsClient.connect(socket, new URI("wss://"+response.endpoint), upgradeRequest);
		return socket;
	}

	public DiscordVoiceWS(VoiceUpdateResponse event, DiscordClientImpl client, WebSocketClient wsClient) throws URISyntaxException {
		this.client = client;
		this.event = event;
		this.guild = client.getGuildByID(event.guild_id);
		this.wsClient = wsClient;
	}

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.session = session;
		send(DiscordUtils.GSON.toJson(new VoiceConnectRequest(event.guild_id, client.ourUser.getID(), client.getWebSocket(event.guild_id).sessionId, event.token)));
		LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Connected to the Discord Voice websocket.");
	}

	@OnWebSocketMessage
	public final void onMessage(Session session, String message) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(message).getAsJsonObject();

		int op = object.get("op").getAsInt();

		switch (op) {
			case OP_INITIAL_CONNECTION: {
				try {
					JsonObject eventObject = (JsonObject) object.get("d");
					ssrc = eventObject.get("ssrc").getAsInt();

					udpSocket = new DatagramSocket();
					addressPort = new InetSocketAddress(event.endpoint, eventObject.get("port").getAsInt());

					ByteBuffer buffer = ByteBuffer.allocate(70);
					buffer.putInt(ssrc);

					DatagramPacket discoveryPacket = new DatagramPacket(buffer.array(), buffer.array().length, addressPort);
					udpSocket.send(discoveryPacket);

					DatagramPacket receivedPacket = new DatagramPacket(new byte[70], 70);
					udpSocket.receive(receivedPacket);

					byte[] data = receivedPacket.getData();

					int ourPort = ((0x000000FF & ((int) data[receivedPacket.getLength()-1])) << 8) | ((0x000000FF & ((int) data[receivedPacket.getLength()-2])));

					String ourIP = new String(data);
					ourIP = ourIP.substring(4, ourIP.length()-2);
					ourIP = ourIP.trim();

					send(DiscordUtils.GSON.toJson(new VoiceUDPConnectRequest(ourIP, ourPort)));

					startKeepalive(eventObject.get("heartbeat_interval").getAsInt());
				} catch (IOException e) {
					LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
				break;
			}
			case OP_HEARTBEAT_RETURN: {
				long timePingSent = object.get("d").getAsLong();
				client.dispatcher.dispatch(new VoicePingEvent((System.currentTimeMillis()-timePingSent)));
				break;
			}
			case OP_CONNECTING_COMPLETED: {
				isConnected.set(true);

				JsonArray array = object.get("d").getAsJsonObject().get("secret_key").getAsJsonArray();
				secret = new byte[array.size()];
				for (int i = 0; i < array.size(); i++)
					secret[i] = (byte) array.get(i).getAsInt();

				setupSendThread();
				setupReceiveThread();
				break;
			}
			case OP_USER_SPEAKING_UPDATE: {
				JsonObject eventObject = (JsonObject) object.get("d");
				boolean isSpeaking = eventObject.get("speaking").getAsBoolean();
				int ssrc = eventObject.get("ssrc").getAsInt();
				String userId = eventObject.get("user_id").getAsString();

				IUser user = client.getUserByID(userId);
				if (user == null) {
					LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: "+object.toString());
					return;
				}

				client.dispatcher.dispatch(new VoiceUserSpeakingEvent(user, ssrc, isSpeaking));
				break;
			}
			default: {
				LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Uncaught voice packet: "+object);
			}
		}
	}

	private void setupSendThread() {
		Runnable sendThread = new Runnable() {
			char seq = 0;
			int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.

			@Override
			public void run() {
				try {
					if (isConnected.get()) {
						byte[] data = guild.getAudioManager().getAudio();
						if (data != null && data.length > 0 && !Discord4J.audioDisabled.get()) {
							client.timer = System.currentTimeMillis();
							AudioPacket packet = new AudioPacket(seq, timestamp, ssrc, data, secret);
							if (!isSpeaking)
								setSpeaking(true);
							udpSocket.send(packet.asUdpPacket(addressPort));

							if (seq+1 > Character.MAX_VALUE)
								seq = 0;
							else
								seq++;

							timestamp += AudioManager.OPUS_FRAME_SIZE;
						} else if (isSpeaking)
							setSpeaking(false);
					}
				} catch (Exception e) {
					LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
			}
		};
		executorService.scheduleWithFixedDelay(sendThread, 0, AudioManager.OPUS_FRAME_TIME_AMOUNT-1, TimeUnit.MILLISECONDS);
	}

	private void setupReceiveThread() {
//		Runnable receiveThread = ()->{
//			if (isConnected.get()) {
//				DatagramPacket receivedPacket = new DatagramPacket(new byte[1920], 1920);
//				try {
//					udpSocket.receive(receivedPacket);
//
//					AudioPacket packet = new AudioPacket(receivedPacket);
//					client.getDispatcher().dispatch(new AudioReceiveEvent(packet));
//				} catch (SocketException e) {
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//		executorService.scheduleAtFixedRate(receiveThread, 0, OPUS_FRAME_TIME_AMOUNT, TimeUnit.MILLISECONDS);
	}

	private void startKeepalive(int hearbeat_interval) {
		Runnable keepAlive = ()->{
			if (this.isConnected.get()) {
				long l = System.currentTimeMillis()-client.timer;
				LOGGER.debug(LogMarkers.KEEPALIVE, "Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
				send(DiscordUtils.GSON.toJson(new VoiceKeepAliveRequest(System.currentTimeMillis())));
				client.timer = System.currentTimeMillis();
			}
		};
		executorService.scheduleAtFixedRate(keepAlive,
				client.timer+hearbeat_interval-System.currentTimeMillis(),
				hearbeat_interval, TimeUnit.MILLISECONDS);
	}

	@OnWebSocketMessage
	public void onMessage(Session session, byte[] buf, int offset, int length) {

		//Converts binary data to readable string data
		try {
			InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(buf));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder sb = new StringBuilder();
			String read;
			while ((read = reader.readLine()) != null) {
				sb.append(read);
			}

			String data = sb.toString();
			reader.close();
			inputStream.close();

			onMessage(session, data);
		} catch (IOException e) {
			LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
		}
	}

	@OnWebSocketClose
	public void onClose(Session session, int code, String reason){
		LOGGER.debug(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket disconnected. Exit Code: {}. Reason: {}.", code, reason);
		disconnect(VoiceDisconnectedEvent.Reason.UNKNOWN);
	}

	@OnWebSocketError
	public void onError(Session session, Throwable e) {
		LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket error, disconnecting...", e);
		if (session == null || !session.isOpen()) {
			disconnect(VoiceDisconnectedEvent.Reason.INIT_ERROR);
		} else {
			disconnect(VoiceDisconnectedEvent.Reason.UNKNOWN);
		}
	}

	/**
	 * Sends a message through the websocket.
	 *
	 * @param message The json message to send.
	 */
	public void send(String message) {
		if (session == null || !session.isOpen()) {
			LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Socket attempting to send a message ({}) without a valid session!", message);
			return;
		}
		if (isConnected.get()) {
			try {
				session.getRemote().sendString(message);
			} catch (IOException e) {
				LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error caught attempting to send a websocket message", e);
			}
		}
	}

	/**
	 * Sends a message through the websocket.
	 *
	 * @param object This object is converted to json and sent to the websocket.
	 */
	public void send(Object object) {
		send(DiscordUtils.GSON.toJson(object));
	}

	/**
	 * Disconnects the client WS.
	 */
	public void disconnect(VoiceDisconnectedEvent.Reason reason) {
		if (isConnected.get()) {
			client.dispatcher.dispatch(new VoiceDisconnectedEvent(reason));
			isConnected.set(false);
			client.voiceConnections.remove(guild);
			executorService.shutdownNow();
			if (udpSocket != null)
				udpSocket.close();
			if (reason != VoiceDisconnectedEvent.Reason.INIT_ERROR) {
				session.close();
			}
			try {
				wsClient.stop();
			} catch (Exception e) {
				LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error caught attempting to close the WebSocketClient!", e);
			}
		}
	}

	/**
	 * Updates the speaking status
	 *
	 * @param speaking: is voice currently being sent
	 */
	public void setSpeaking(boolean speaking) {
		this.isSpeaking = speaking;

		send(DiscordUtils.GSON.toJson(new VoiceSpeakingRequest(speaking)));
	}
}
