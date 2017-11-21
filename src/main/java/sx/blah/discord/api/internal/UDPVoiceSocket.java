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
import sx.blah.discord.util.HighPrecisionRecurrentTask;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Facilitates the sending and receiving of voice data on a UDP socket with Discord.
 */
public class UDPVoiceSocket {

	/**
	 * The audio frames sent to Discord when the bot stops sending audio data.
	 * @see <a href=https://discordapp.com/developers/docs/topics/voice-connections#voice-data-interpolation>Voice Data Interpolation</a>
	 */
	private static final byte[] SILENCE_FRAMES = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};
	/**
	 * Data sent on the UDP socket to prevent the port from being reclaimed. This specific packet is ignored by Discord.
	 */
	private static final byte[] KEEP_ALIVE_DATA = {(byte) 0xC9, 0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * The maximum size in bytes of an audio packet received on the socket.
	 */
	private static final int MAX_INCOMING_AUDIO_PACKET = OpusUtil.OPUS_FRAME_SIZE * 2 + 12; //two channels + 12 rtp header bytes.

	/**
	 * The voice gateway associated with this socket.
	 */
	private volatile DiscordVoiceWS voiceWS;
	/**
	 * The underlying udp socket that data is sent and received on.
	 */
	private volatile DatagramSocket udpSocket;
	/**
	 * The socket address data is sent to.
	 */
	private volatile InetSocketAddress address;

	/**
	 * The thread send and receive logic is handled on.
	 */
	private volatile HighPrecisionRecurrentTask audioTask;

	/**
	 * The secret used for encryption.
	 */
	private volatile byte[] secret;
	/**
	 * Whether or not audio is currently being sent on this socket.
	 */
	private volatile boolean isSpeaking = false;
	/**
	 * Incremented for every packet sent .
	 */
	private volatile char sequence = 0;
	/**
	 * Incremented by {@link OpusUtil#OPUS_FRAME_TIME} for every packet sent.
	 */
	private volatile int timestamp = 0;
	/**
	 * The ssrc associated with our user.
	 */
	private volatile int ssrc;

	/**
	 * The number of silence frames left to send after the transmission of audio has stopped.
	 */
	private volatile int silenceToSend = 5;

	/**
	 * Indicates whether or not the {@link #begin()} method has been called. This is used in conjunction with
	 * {@link #wasShutdown} to ensure that {@link #begin()} is called before {@link #shutdown()}.
 	 */
	private volatile boolean hasBegun = false;
	/**
	 * Indicates whether or not the {@link #shutdown()} method has been called. This is used in conjunction with
	 * {@link #hasBegun} to ensure that {@link #begin()} is called before {@link #shutdown()}.
	 */
	private volatile boolean wasShutdown = false;

	/**
	 * Function executed on the {@link #audioTask} for sending audio data.
	 */
	private final Runnable sendRunnable = () -> {
		if (!wasShutdown) {
			try {
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
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
			}
		}
	};

	/**
	 * Function executed on the {@link #audioTask} for receiving audio data.
	 */
	private final Runnable receiveRunnable = () -> {
		if (!wasShutdown) {
			//keep receiving audio for 10ms. This will consume as many frames as are readily available, without blocking the thread for more than 10ms.
			long start = System.nanoTime();
			while (System.nanoTime() - start < 10_000_000) {
				try {
					DatagramPacket udpPacket = new DatagramPacket(new byte[MAX_INCOMING_AUDIO_PACKET], MAX_INCOMING_AUDIO_PACKET);
					udpSocket.receive(udpPacket);

					OpusPacket opus = new OpusPacket(udpPacket);
					opus.decrypt(secret);

					IUser user = voiceWS.users.get(opus.header.ssrc);
					if (user != null) {
						((AudioManager) voiceWS.getGuild().getAudioManager()).receiveAudio(opus.getAudio(), user, opus.header.sequence, opus.header.timestamp);
					}
				} catch (SocketTimeoutException ignored) {
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
				}
			}
		}
	};

	/**
	 * Function executed on the {@link #audioTask} for keeping the udp socket alive.
	 */
	private final Runnable keepAliveRunnable = new Runnable() {
		int iterations = 0;

		@Override
		public void run() {
			if (!wasShutdown) {
				iterations++;
				if (iterations % (5000 / 20) == 0) { //once every 5 seconds, assuming that each invocation happens every 20ms.
					try {
						DatagramPacket packet = new DatagramPacket(KEEP_ALIVE_DATA, KEEP_ALIVE_DATA.length, address);
						udpSocket.send(packet);
					} catch (Exception e) {
						Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Internal exception sending UDP keepalive: ", e);
					}
				}
			}
		}
	};

	UDPVoiceSocket(DiscordVoiceWS voiceWS) {
		this.voiceWS = voiceWS;
	}

	/**
	 * Called when the voice gateway receives {@link VoiceOps#READY}. This performs IP discovery and sends
	 * {@link VoiceOps#SELECT_PROTOCOL} on the voice gateway.
	 *
	 * @param endpoint The endpoint to send audio data to.
	 * @param port The port to send audio data on.
	 * @param ssrc The self user's ssrc.
	 */
	synchronized void setup(String endpoint, int port, int ssrc) {
		try {
			this.udpSocket = new DatagramSocket();
			this.address = new InetSocketAddress(endpoint, port);
			this.ssrc = ssrc;

			Pair<String, Integer> ourIp = doIPDiscovery(ssrc);

			udpSocket.setSoTimeout(5); //after IP discovery, every usage times out after 5ms, because it's better to drop a frame than block the thread.

			SelectProtocolRequest selectRequest = new SelectProtocolRequest(ourIp.getLeft(), ourIp.getRight());
			voiceWS.send(VoiceOps.SELECT_PROTOCOL, selectRequest);
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error opening voice UDP socket: ", e);
		}
	}

	/**
	 * Called when {@link VoiceOps#SESSION_DESCRIPTION} is received on the voice gateway. At this point, the connection
	 * handshake has been completed and all of the necessary information for sending and receiving voice data has been
	 * received.
	 *
	 * <p><b>If this socket is not in the correct state according to {@link #hasBegun} and {@link #wasShutdown}, a call to
	 * this method will be ignored.</b>
	 */
	synchronized void begin() {
		if (!hasBegun && !wasShutdown) {
			hasBegun = true;
			audioTask = new HighPrecisionRecurrentTask(OpusUtil.OPUS_FRAME_TIME, 0.01f, () -> {
				synchronized (udpSocket) { //while the the audio handling is happening, lock the socket so no concurrent shutdown happens
					if (!udpSocket.isClosed()) {
						sendRunnable.run();
						receiveRunnable.run();
						keepAliveRunnable.run();
					}
				}
			});
			audioTask.setDaemon(true);
			audioTask.start();
		}
	}

	/**
	 * Called when {@link DiscordWS#shutdown()} is called.
	 *
	 * <p><b>If this socket is not in the correct state according to {@link #hasBegun} and {@link #wasShutdown}, a call to
	 * this method will be ignored.</b>
	 */
	synchronized void shutdown() {
		if (hasBegun && !wasShutdown) {
			wasShutdown = true;
			audioTask.setStop(true);
			synchronized (udpSocket) {
				udpSocket.close();
			}
		}
	}

	/**
	 * Obtains the machine's external IP address and port to send to Discord.
	 *
	 * @param ssrc The self user's ssrc.
	 * @return A pair of the machine's external IP address and port.
	 * @throws IOException Thrown by underlying UDP socket.
	 *
	 * @see <a href=https://discordapp.com/developers/docs/topics/voice-connections#ip-discovery>IP Discovery</a>
	 */
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

	/**
	 * Sets {@link #isSpeaking} and sends {@link VoiceOps#SPEAKING} on the voice gateway.
	 *
	 * @param isSpeaking Whether audio data is being sent or not.
	 */
	private void setSpeaking(boolean isSpeaking) {
		this.isSpeaking = isSpeaking;
		voiceWS.send(VoiceOps.SPEAKING, new VoiceSpeakingRequest(isSpeaking));
	}

	/**
	 * Sends {@link #SILENCE_FRAMES} on the voice socket.
	 *
	 * @throws IOException Thrown by the underlying UDP socket.
	 */
	private void sendSilence() throws IOException {
		OpusPacket packet = new OpusPacket(sequence, timestamp, ssrc, SILENCE_FRAMES);
		packet.encrypt(secret);
		byte[] toSend = packet.asByteArray();

		udpSocket.send(new DatagramPacket(toSend, toSend.length, address));
	}

	/**
	 * Sets the secret used for voice encryption and decryption.
	 *
	 * @param secret The secret used for voice encryption and decryption.
	 */
	void setSecret(byte[] secret) {
		this.secret = secret;
	}
}
