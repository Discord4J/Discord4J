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

import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.requests.voice.SelectProtocolRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceSpeakingRequest;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPVoiceSocket {

	private static final byte[] SILENCE_FRAMES = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};

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

	private int silenceToSend = 5;

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
					silenceToSend = 5;
				} else {
					if (isSpeaking) setSpeaking(false);
					if (silenceToSend > 0) {
						sendSilence();
						silenceToSend--;
					}
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

				OpusPacket opusPacket = new OpusPacket(udpPacket);
				opusPacket.decrypt(secret);

				IUser userSpeaking = voiceWS.users.get(opusPacket.header.ssrc);
				if (userSpeaking != null) {
					((AudioManager) voiceWS.getGuild().getAudioManager()).receiveAudio(opusPacket.getAudio(), userSpeaking);
				}
			}
		} catch (IOException e) {
			if (e instanceof SocketException) return;
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
		receiveExecutor.shutdown();
		sendExecutor.shutdown();
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

	private void sendSilence() throws IOException {
		OpusPacket packet = new OpusPacket(sequence, timestamp, ssrc, SILENCE_FRAMES);
		packet.encrypt(secret);
		byte[] toSend = packet.asByteArray();

		udpSocket.send(new DatagramPacket(toSend, toSend.length, address));
	}

	void setSecret(byte[] secret) {
		this.secret = secret;
	}
}
