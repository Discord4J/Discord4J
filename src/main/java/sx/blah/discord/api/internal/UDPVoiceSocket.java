package sx.blah.discord.api.internal;

import org.apache.commons.lang3.tuple.Pair;
import org.peergos.crypto.TweetNaCl;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.requests.voice.SelectProtocolRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceSpeakingRequest;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPVoiceSocket {

	private DiscordVoiceWS voiceWS;
	private DatagramSocket udpSocket;
	private InetSocketAddress address;

	private final ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Audio Send Executor"));
	private final ScheduledExecutorService receiveExecutor = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Audio Receive Executor"));

	private byte[] secret;
	private boolean isSpeaking = false;
	private char sequence = 0;
	private int timestamp = 0;
	private int ssrc;

	private final Runnable sendRunnable = () -> {
		try {
			if (!udpSocket.isClosed()) {
				byte[] audio = ((AudioManager) voiceWS.getGuild().getAudioManager()).sendAudio();
				if (audio != null && audio.length > 0) {
					OpusPacket packet = new OpusPacket(sequence, timestamp, ssrc, audio);
					packet.encrypt(secret);
					byte[] toSend = packet.asByteArray();

					if (!isSpeaking) setSpeaking(true);
					udpSocket.send(new DatagramPacket(toSend, toSend.length, address));

					sequence++;
					timestamp += OpusUtil.OPUS_FRAME_SIZE;
				} else if (isSpeaking) {
					setSpeaking(false);
				}
			}
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
		}
	};

	private final Runnable receiveRunnable = () -> {
		try {
			if (!udpSocket.isClosed()) {
				DatagramPacket udpPacket = new DatagramPacket(new byte[1920], 1920);
				udpSocket.receive(udpPacket);

				/*
				System.out.println(Arrays.toString(udpPacket.getData()));

				OpusPacket opusPacket = new OpusPacket(udpPacket);
				opusPacket.decrypt(secret);

				System.out.println(Arrays.toString(opusPacket.asByteArray()));

				IUser userSpeaking = voiceWS.users.get(opusPacket.header.ssrc);
				if (userSpeaking != null) {
					((AudioManager) voiceWS.getGuild().getAudioManager()).receiveAudio(opusPacket.asByteArray(), userSpeaking);
				}
				*/

				System.out.println(Arrays.toString(secret));

				byte[] data = udpPacket.getData();
				byte[] nonce = new byte[24];
				byte[] audio = new byte[data.length - 12];

				System.arraycopy(data, 0, nonce, 0, 12);
				System.arraycopy(data, 12, audio, 0, audio.length);

				//byte[] decrypted = new TweetNaclFast.SecretBox(secret).open(audio, nonce);
				byte[] decrypted = TweetNaCl.secretbox_open(audio, nonce, secret);
				System.out.println(Arrays.toString(decrypted));

				/*
				byte[] ary = udpPacket.getData();
				byte[] header = Arrays.copyOfRange(ary, 0, 12);
				byte[] nonce = ArrayUtils.addAll(header, new byte[12]);
				byte[] audio = new byte[ary.length - 12];
				TweetNaCl.secretbox_open(audio, nonce, secret);
				*/

				/*
				System.out.println(Arrays.toString(udpPacket.getData()));
				System.out.println(Arrays.toString(Arrays.copyOf(udpPacket.getData(), OpusPacket.RTPHeader.LENGTH)));
				System.out.println(Arrays.toString(Arrays.copyOfRange(udpPacket.getData(), OpusPacket.RTPHeader.LENGTH, udpPacket.getData().length)));
				*/

			}
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
		}
	};

	UDPVoiceSocket(DiscordVoiceWS voiceWS) {
		this.voiceWS = voiceWS;
	}

	void setup(String endpoint, int port, int ssrc) throws IOException {
		try {
			this.udpSocket = new DatagramSocket();
			this.address = new InetSocketAddress(endpoint, port);
			this.ssrc = ssrc;

			Pair<String, Integer> ourIp = doIPDiscovery(ssrc);

			SelectProtocolRequest selectRequest = new SelectProtocolRequest(ourIp.getLeft(), ourIp.getRight());
			voiceWS.send(VoiceOps.SELECT_PROTOCOL, selectRequest);
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error opening voice UDP socket: ", e);
		}
	}

	void begin() {
		sendExecutor.scheduleWithFixedDelay(sendRunnable, 0, OpusUtil.OPUS_FRAME_TIME - 1, TimeUnit.MILLISECONDS);
		receiveExecutor.scheduleWithFixedDelay(receiveRunnable, 0, OpusUtil.OPUS_FRAME_TIME, TimeUnit.MILLISECONDS);
	}

	void shutdown() {
		udpSocket.close();
	}

	private Pair<String, Integer> doIPDiscovery(int ssrc) throws IOException {
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

	private void setSpeaking(boolean isSpeaking) {
		this.isSpeaking = isSpeaking;
		voiceWS.send(VoiceOps.SPEAKING, new VoiceSpeakingRequest(isSpeaking));
	}

	void setSecret(byte[] secret) {
		this.secret = secret;
	}
}
