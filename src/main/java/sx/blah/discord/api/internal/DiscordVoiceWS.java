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
import sx.blah.discord.handle.impl.events.AudioReceiveEvent;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.VoicePingEvent;
import sx.blah.discord.handle.impl.events.VoiceUserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.json.requests.VoiceConnectRequest;
import sx.blah.discord.json.requests.VoiceKeepAliveRequest;
import sx.blah.discord.json.requests.VoiceSpeakingRequest;
import sx.blah.discord.json.requests.VoiceUDPConnectRequest;
import sx.blah.discord.json.responses.VoiceUpdateResponse;
import sx.blah.discord.util.LogMarkers;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterInputStream;

@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE, maxIdleTime = Integer.MAX_VALUE, maxTextMessageSize = Integer.MAX_VALUE)
public class DiscordVoiceWS {

	// OP codes
	private static final int OP_INITIAL_CONNECTION = 2;
	private static final int OP_HEARTBEAT_RETURN = 3;
	private static final int OP_CONNECTING_COMPLETED = 4;
	private static final int OP_USER_SPEAKING_UPDATE = 5;

	private AtomicBoolean isConnected = new AtomicBoolean(true);
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		private volatile int executorCount = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("Discord4J Voice WebSocket Client Executor "+(executorCount++));
			return thread;
		}
	});

	/**
	 * The client associated with this WS.
	 */
	private DiscordClientImpl client;

	/**
	 * The guild associated with this WS.
	 */
	private IGuild guild;

	/**
	 * The response from the join voice channel request.
	 */
	private VoiceUpdateResponse joinResponse;

	/**
	 * The SSRC that has been assigned to us by Discord.
	 * This is a unique number used in RTP to identify the source of a packet.
	 */
	private int ourSSRC;

	/**
	 * The UDP Socket to send and receive audio data on.
	 */
	private DatagramSocket udpSocket;

	/**
	 * The address to send data to.
	 */
	private InetSocketAddress addressPort;

	/**
	 * If the client associated with this WS should be shown as speaking.
	 */
	private boolean isSpeaking;

	/**
	 * Secret key sent by Discord for use in encryption/decryption of audio packets.
	 */
	private byte[] secret;

	/**
	 * The session used by this WS.
	 */
	private Session session;

	/**
	 * A map associating IUsers to their respective Ssrcs.
	 * Each user in a voice channel has a unique Ssrc so we can look up which user is speaking using this map.
	 */
	HashMap<Integer, IUser> userSsrcs = new HashMap<>();

	static DiscordVoiceWS connect(VoiceUpdateResponse response, IDiscordClient client) throws Exception {
		SslContextFactory sslFactory = new SslContextFactory();
		WebSocketClient wsClient = new WebSocketClient(sslFactory);
		wsClient.setDaemon(true);
		DiscordVoiceWS socket = new DiscordVoiceWS(response, (DiscordClientImpl) client);
		wsClient.start();
		ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
		upgradeRequest.setHeader("Accept-Encoding", "gzip, deflate");
		wsClient.connect(socket, new URI("wss://"+response.endpoint), upgradeRequest);
		return socket;
	}

	DiscordVoiceWS(VoiceUpdateResponse response, DiscordClientImpl client) throws URISyntaxException {
		this.client = client;
		this.joinResponse = response;
		this.guild = client.getGuildByID(response.guild_id);
	}

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.session = session;
		send(DiscordUtils.GSON.toJson(new VoiceConnectRequest(joinResponse.guild_id, client.ourUser.getID(), client.sessionId, joinResponse.token)));
		Discord4J.LOGGER.info(LogMarkers.VOICE_WEBSOCKET, "Connected to the Discord Voice websocket.");
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
					ourSSRC = eventObject.get("ssrc").getAsInt();

					udpSocket = new DatagramSocket();
					addressPort = new InetSocketAddress(joinResponse.endpoint, eventObject.get("port").getAsInt());

					ByteBuffer buffer = ByteBuffer.allocate(70);
					buffer.putInt(ourSSRC);

					DatagramPacket discoveryPacket = new DatagramPacket(buffer.array(), buffer.array().length, addressPort);
					udpSocket.send(discoveryPacket);

					DatagramPacket receivedPacket = new DatagramPacket(new byte[70], 70);
					udpSocket.receive(receivedPacket); // Wait to receive the response to the discovery packet.

					byte[] data = receivedPacket.getData();

					int ourPort = ((0x000000FF & ((int) data[receivedPacket.getLength()-1])) << 8) | ((0x000000FF & ((int) data[receivedPacket.getLength()-2])));

					String ourIP = new String(data);
					ourIP = ourIP.substring(4, ourIP.length()-2);
					ourIP = ourIP.trim();

					send(DiscordUtils.GSON.toJson(new VoiceUDPConnectRequest(ourIP, ourPort)));

					startKeepAlive(eventObject.get("heartbeat_interval").getAsInt()); // Start the keep alive with the interval discord sent us.
				} catch (IOException e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
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
				boolean isSpeaking = eventObject.get("speaking").getAsBoolean(); // TODO: It might be helpful to store this
				int ssrc = eventObject.get("ssrc").getAsInt();
				String userId = eventObject.get("user_id").getAsString();

				IUser user = client.getUserByID(userId);
				if (user == null) {
					Discord4J.LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: "+object.toString());
					return;
				}

				userSsrcs.put(ssrc, user);
				client.dispatcher.dispatch(new VoiceUserSpeakingEvent(user, ssrc, isSpeaking));
				break;
			}
			default: Discord4J.LOGGER.warn(LogMarkers.VOICE_WEBSOCKET, "Uncaught voice packet: " + object);
		}
	}

	/**
	 * Sets up the thread for sending audio packets on the Discord UDP Socket.
	 */
	private void setupSendThread() {
		Runnable sendThread = new Runnable() {
			char seq = 0;      // Incremented by 1 for each packet received. Used to detect packet loss
			int timestamp = 0; // Used to sync up our packets within the same timeframe of other people talking.

			@Override
			public void run() {
				try {
					if (isConnected.get()) {
						byte[] encodedAudio = guild.getAudioManager().getAudio(); // The audio in the queue that needs to be sent.
						if (encodedAudio != null && encodedAudio.length > 0) {
							client.timer = System.currentTimeMillis();
							AudioPacket packet = AudioPacket.fromEncodedAudio(seq, timestamp, ourSSRC, encodedAudio);

							if (!isSpeaking) setSpeaking(true);

							udpSocket.send(packet.encrypt(secret).asUdpPacket(addressPort));

							if (seq + 1 > Character.MAX_VALUE) {
								seq = 0;
							} else {
								seq++;
							}

							timestamp += OpusUtil.OPUS_FRAME_SIZE;
						} else if (isSpeaking) {
							// There is no audio that needs sending so if we were speaking previously, stop.
							setSpeaking(false);
						}
					}
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
			}
		};
		executorService.scheduleAtFixedRate(sendThread, 0, OpusUtil.OPUS_FRAME_TIME_AMOUNT, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sets up the thread for receiving audio packets from the Discord UDP Socket.
	 */
	private void setupReceiveThread() {
		Runnable receiveThread = () -> {
			if (isConnected.get()) {
				DatagramPacket receivedPacket = new DatagramPacket(new byte[1920], 1920);
				try {
					udpSocket.receive(receivedPacket); // This blocks the thread until a packet is received.
					AudioPacket packet = AudioPacket.fromUdpPacket(receivedPacket).decrypt(secret);

					IUser userSpeaking = userSsrcs.get(packet.getSsrc());
					// We don't have a user associated with this user. This is probably the first time they have spoken since the bot/they joined. Ignore for now.
					if (userSpeaking != null) {
						byte[] decodedAudio = OpusUtil.decodeToPCM(packet.getEncodedAudio(), 2, userSpeaking); // TODO: Detect if mono

						// TODO: Austin software design magic needed!
						// TODO: Create combined audio stream of multiple users. I feel this is too dependent on the actual implementation of IAudioReceiver to do right now.
						client.getDispatcher().dispatch(new AudioReceiveEvent(userSpeaking, decodedAudio));
					}
				} catch (IOException e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
				}
			}
		};
		executorService.scheduleAtFixedRate(receiveThread, 0, OpusUtil.OPUS_FRAME_TIME_AMOUNT, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sets up the keep alive thread.
	 * @param heartbeat_interval The interval on which to send the request to Discord in milliseconds.
	 */
	private void startKeepAlive(int heartbeat_interval) {
		Runnable keepAlive = () -> {
			if (this.isConnected.get()) {
				long elapsedTime = System.currentTimeMillis() - client.timer;
				Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), elapsedTime);
				send(DiscordUtils.GSON.toJson(new VoiceKeepAliveRequest(System.currentTimeMillis())));
				client.timer = System.currentTimeMillis();
			}
		};
		executorService.scheduleAtFixedRate(keepAlive,
				client.timer + heartbeat_interval - System.currentTimeMillis(),
				heartbeat_interval, TimeUnit.MILLISECONDS);
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
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord Internal Exception", e);
		}
	}

	@OnWebSocketClose
	public void onClose(Session session, int code, String reason){
		Discord4J.LOGGER.debug(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket disconnected. Exit Code: {}. Reason: {}.", code, reason);
		disconnect(VoiceDisconnectedEvent.Reason.UNKNOWN);
	}

	@OnWebSocketError
	public void onError(Session session, Throwable e) {
		Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Voice Websocket error, disconnecting...", e);
		if (session == null || !session.isOpen()) {
			disconnect(VoiceDisconnectedEvent.Reason.INIT_ERROR);
		} else {
			disconnect(VoiceDisconnectedEvent.Reason.UNKNOWN);
		}
	}

	/**
	 * Sends a message through the websocket.
	 * @param message The json message to send.
	 */
	public void send(String message) {
		if (session == null || !session.isOpen()) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Socket attempting to send a message ({}) without a valid session!", message);
			return;
		}
		if (isConnected.get()) {
			try {
				session.getRemote().sendString(message);
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Error caught attempting to send a websocket message", e);
			}
		}
	}

	/**
	 * Sends a message through the websocket.
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

			// Some clean up
			isConnected.set(false);
			client.voiceConnections.remove(guild);
			executorService.shutdownNow();

			if (udpSocket != null) udpSocket.close();
			if (reason != VoiceDisconnectedEvent.Reason.INIT_ERROR) session.close();
		}
	}

	/**
	 * Updates the speaking status.
	 * @param speaking: True if the client should be shown as speaking.
	 */
	private void setSpeaking(boolean speaking) {
		this.isSpeaking = speaking;
		send(DiscordUtils.GSON.toJson(new VoiceSpeakingRequest(speaking)));
	}
}
