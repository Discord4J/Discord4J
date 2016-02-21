package sx.blah.discord.api.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.audio.AudioPacket;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.VoicePingEvent;
import sx.blah.discord.handle.impl.events.VoiceUserSpeakingEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.json.requests.KeepAliveRequest;
import sx.blah.discord.json.requests.VoiceConnectRequest;
import sx.blah.discord.json.requests.VoiceSpeakingRequest;
import sx.blah.discord.json.requests.VoiceUDPConnectRequest;
import sx.blah.discord.json.responses.VoiceUpdateResponse;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterInputStream;

public class DiscordVoiceWS extends WebSocketClient {

	public static final int OPUS_SAMPLE_RATE = 48000;   //(Hz) We want to use the highest of qualities! All the bandwidth!
	public static final int OPUS_FRAME_SIZE = 960;
	public static final int OPUS_FRAME_TIME_AMOUNT = OPUS_FRAME_SIZE*1000/OPUS_SAMPLE_RATE;
	public static final int OPUS_CHANNEL_COUNT = 2;     //Stereo audio channel

	public static final int OP_INITIAL_CONNECTION = 2;
	public static final int OP_HEARTBEAT_RETURN = 3;
	public static final int OP_CONNECTING_COMPLETED = 4;
	public static final int OP_USER_SPEAKING_UPDATE = 5;

	private static final HashMap<String, String> headers = new HashMap<>();

	static {
		headers.put("Accept-Encoding", "gzip");
	}

	public AtomicBoolean isConnected = new AtomicBoolean(false);
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

	private DiscordClientImpl client;

	private static int ssrc;
	private static VoiceUpdateResponse event;
	private static DatagramSocket udpSocket;

	private InetSocketAddress addressPort;
	private boolean isSpeaking;

	public DiscordVoiceWS(VoiceUpdateResponse event, DiscordClientImpl client) throws URISyntaxException {
		super(new URI("wss://"+event.endpoint), new Draft_10(), headers, 0);
		this.client = client;
		DiscordVoiceWS.event = event;
		try {
			super.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(SSLContext.getDefault()));
			this.connect();
		} catch (NoSuchAlgorithmException e) {
			Discord4J.LOGGER.error("Error setting up SSL connection!", e);
		}
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		send(DiscordUtils.GSON.toJson(new VoiceConnectRequest(event.guild_id, client.ourUser.getID(), client.sessionId, event.token)));
		Discord4J.LOGGER.info("Connected to the Discord Voice websocket.");
	}

	@Override
	public final void onMessage(String frame) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(frame).getAsJsonObject();

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
					Discord4J.LOGGER.error("Discord Internal Exception", e);
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
					Discord4J.LOGGER.warn("Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: "+object.toString());
					return;
				}

				client.dispatcher.dispatch(new VoiceUserSpeakingEvent(user, ssrc, isSpeaking));
				break;
			}
			default: {
				Discord4J.LOGGER.warn("Uncaught voice packet: "+object);
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
						byte[] rawAudio = client.audioChannel.getAudioData(OPUS_FRAME_SIZE);
						if (rawAudio != null) {
							client.timer = System.currentTimeMillis();
							AudioPacket packet = new AudioPacket(seq, timestamp, ssrc, rawAudio);
							if (!isSpeaking)
								setSpeaking(true);
							udpSocket.send(packet.asUdpPacket(addressPort));

							if (seq+1 > Character.MAX_VALUE)
								seq = 0;
							else
								seq++;

							timestamp += OPUS_FRAME_SIZE;
						} else if (isSpeaking)
							setSpeaking(false);
					}
				} catch (IOException e) {
					Discord4J.LOGGER.error("Discord Internal Exception", e);
				}
			}
		};
		executorService.scheduleAtFixedRate(sendThread, 0, OPUS_FRAME_TIME_AMOUNT, TimeUnit.MILLISECONDS);
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
				Discord4J.LOGGER.debug("Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
				send(DiscordUtils.GSON.toJson(new KeepAliveRequest(3)));
				client.timer = System.currentTimeMillis();
			}
		};
		executorService.scheduleAtFixedRate(keepAlive,
				client.timer+hearbeat_interval-System.currentTimeMillis(),
				hearbeat_interval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onMessage(ByteBuffer bytes) {

		//Converts binary data to readable string data
		try {
			InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes.array()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder sb = new StringBuilder();
			String read;
			while ((read = reader.readLine()) != null) {
				sb.append(read);
			}

			String data = sb.toString();
			reader.close();
			inputStream.close();

			onMessage(data);
		} catch (IOException e) {
			Discord4J.LOGGER.error("Discord Internal Exception", e);
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
//		System.out.println(s);
	}

	@Override
	public void onError(Exception e) {
		Discord4J.LOGGER.error("Discord Internal Exception", e);
	}

	@Override
	public void send(String text) throws NotYetConnectedException {
		try {
			super.send(text);
		} catch (WebsocketNotConnectedException e) {
			Discord4J.LOGGER.warn("Voice Websocket unexpectedly lost connection!");
			disconnect(VoiceDisconnectedEvent.Reason.UNKNOWN);
		}
	}

	/**
	 * Disconnects the client WS.
	 */
	public synchronized void disconnect(VoiceDisconnectedEvent.Reason reason) {
		client.dispatcher.dispatch(new VoiceDisconnectedEvent(reason));
		isConnected.set(false);
		udpSocket.close();
		close();
		Thread.currentThread().interrupt();

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
