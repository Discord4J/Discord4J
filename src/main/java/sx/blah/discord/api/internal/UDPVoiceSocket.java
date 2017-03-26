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
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class UDPVoiceSocket {

	private static final byte[] SILENCE_FRAMES = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};
	private static final byte[] KEEP_ALIVE_DATA = {(byte) 0xC9, 0, 0, 0, 0, 0, 0, 0, 0};
	private static final int MAX_INCOMING_AUDIO_PACKET = OpusUtil.OPUS_FRAME_SIZE * 2 + 12; //two channels + 12 rtp header bytes.

	private DiscordVoiceWS voiceWS;
	private DatagramSocket udpSocket;
	private InetSocketAddress address;

	private HighPrecisionRecurrentTask audioTask;

	private byte[] secret;
	private boolean isSpeaking = false;
	private char sequence = 0;
	private int timestamp = 0;
	private int ssrc;

	private int silenceToSend = 5;

	private final Runnable sendRunnable = () -> {
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
	};

	private final Runnable receiveRunnable = new Runnable() {
		int iterations = 0;

		@Override
		public void run() {
			iterations++;
			if (iterations % (5000 / 20) == 0) { //once every 5 seconds, assuming that each invocation happens every 20ms.
				try {
					DatagramPacket udpPacket = new DatagramPacket(new byte[MAX_INCOMING_AUDIO_PACKET], MAX_INCOMING_AUDIO_PACKET);
					udpSocket.receive(udpPacket);

					OpusPacket opusPacket = new OpusPacket(udpPacket);
					opusPacket.decrypt(secret);

					IUser userSpeaking = voiceWS.users.get(opusPacket.header.ssrc);
					if (userSpeaking != null) {
						((AudioManager) voiceWS.getGuild().getAudioManager()).receiveAudio(opusPacket.getAudio(), userSpeaking);
					}
				} catch (SocketTimeoutException e) {
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
				}
			}
		}
	};

	private final Runnable keepAliveRunnable = () -> {
		try {
			DatagramPacket packet = new DatagramPacket(KEEP_ALIVE_DATA, KEEP_ALIVE_DATA.length, address);
			udpSocket.send(packet);
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Internal exception sending UDP keepalive: ", e);
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
			
			udpSocket.setSoTimeout(10); //after IP discovery, every usage times out after 10ms, because it's better to drop a frame than block the thread.

			SelectProtocolRequest selectRequest = new SelectProtocolRequest(ourIp.getLeft(), ourIp.getRight());
			voiceWS.send(VoiceOps.SELECT_PROTOCOL, selectRequest);
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Encountered error opening voice UDP socket: ", e);
		}
	}

	void begin() {
		audioTask = new HighPrecisionRecurrentTask(OpusUtil.OPUS_FRAME_TIME, 0.01f, () -> {
			if (!udpSocket.isClosed()) {
				sendRunnable.run();
				receiveRunnable.run();
				keepAliveRunnable.run();
			}
		});
		audioTask.setDaemon(true);
		audioTask.start();
	}

	void shutdown() {
		audioTask.setStop(true);
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

/**
 * Custom Thread that executes the passed task every specified period.
 *
 * Time of execution is held in account when calculating the time to sleep until the next execution.
 *
 */
class HighPrecisionRecurrentTask extends Thread {

	private final int periodInNanos;
	private final int spinningTimeNanos;
	private final Runnable target;
	private volatile boolean stop;

	/**
	 *
	 * @param periodInMilis Every what time the task is executed.
	 * @param percentageOfSleepSpinning Of the total millis to sleep, what percentage of it should be done spinning to increase accuracy.
	 * @param target Task to run
	 */
	public HighPrecisionRecurrentTask(int periodInMilis, float percentageOfSleepSpinning, Runnable target) {
		super("D4J AudioThread");
		if (percentageOfSleepSpinning < 0 || percentageOfSleepSpinning > 1)
			throw new IllegalArgumentException("percentageOfSleepSpinning < 0 | percentageOfSleepSpinning > 1");
		this.periodInNanos = periodInMilis * 1_000_000;
		this.spinningTimeNanos = (int) (periodInNanos * percentageOfSleepSpinning);
		this.target = target;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	@Override
	@SuppressWarnings("empty-statement")
	public void run() {
		long nextTarget = 0;
		while (!stop) {
			long now = System.nanoTime();
			target.run();
			long total = System.nanoTime() - now;
			nextTarget = now + periodInNanos - total;
			sleepFor(periodInNanos - total - spinningTimeNanos);
			while (nextTarget > System.nanoTime()); //consume cycles
		}
	}

	private void sleepFor(long nanos) {
		if (nanos > 0) {
			long ms = nanos / 1000000;
			long nanosRem = nanos % 1000000;
			try {
				sleep(ms, (int) nanosRem);
			} catch (InterruptedException ex) {
			}
		}
	}

}
