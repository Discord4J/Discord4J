/**
 * Copyright 2015-2016 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sx.blah.discord.api.internal.audio;

import com.sun.jna.ptr.PointerByReference;
import sx.blah.discord.api.internal.DiscordVoiceWS;
import sx.blah.discord.util.Opus;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * Represents the contents of a audio packet that was either received from Discord or
 * will be sent to discord.
 */
public class AudioPacket {

	private static PointerByReference opusEncoder;
	private static PointerByReference opusDecoder;

	static {
		try {
			IntBuffer error = IntBuffer.allocate(4);
			opusEncoder = Opus.INSTANCE.opus_encoder_create(DiscordVoiceWS.OPUS_SAMPLE_RATE, DiscordVoiceWS.OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);

			error = IntBuffer.allocate(4);
			opusDecoder = Opus.INSTANCE.opus_decoder_create(DiscordVoiceWS.OPUS_SAMPLE_RATE, DiscordVoiceWS.OPUS_CHANNEL_COUNT, error);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			opusEncoder = null;
			opusDecoder = null;
		}
	}

	private final char seq;
	private final int timestamp;
	private final int ssrc;
	private final byte[] encodedAudio;
	private final byte[] rawPacket;
	private final byte[] rawAudio;

	public AudioPacket(DatagramPacket packet) {
		this(Arrays.copyOf(packet.getData(), packet.getLength()));
	}

	public AudioPacket(byte[] rawPacket) {
		this.rawPacket = rawPacket;

		ByteBuffer buffer = ByteBuffer.wrap(rawPacket);
		this.seq = buffer.getChar(2);
		this.timestamp = buffer.getInt(4);
		this.ssrc = buffer.getInt(8);

		byte[] audio = new byte[buffer.array().length-12];
		System.arraycopy(buffer.array(), 12, audio, 0, audio.length);
		this.encodedAudio = audio;
		this.rawAudio = decodeToPCM(encodedAudio);
	}

	public AudioPacket(char seq, int timestamp, int ssrc, byte[] rawAudio) {
		this.seq = seq;
		this.ssrc = ssrc;
		this.timestamp = timestamp;
		this.rawAudio = rawAudio;
		this.encodedAudio = AudioPacket.encodeToOpus(rawAudio);


		ByteBuffer buffer = ByteBuffer.allocate(encodedAudio.length+12);
		buffer.put(0, (byte) 0x80);
		buffer.put(1, (byte) 0x78);
		buffer.putChar(2, seq);
		buffer.putInt(4, timestamp);
		buffer.putInt(8, ssrc);
		System.arraycopy(encodedAudio, 0, buffer.array(), 12, encodedAudio.length);//12 - n
		this.rawPacket = buffer.array();
	}

	public byte[] getRawPacket() {
		return Arrays.copyOf(rawPacket, rawPacket.length);
	}

	public DatagramPacket asUdpPacket(InetSocketAddress address) {
		return new DatagramPacket(getRawPacket(), rawPacket.length, address);
	}

	public static byte[] encodeToOpus(byte[] rawAudio) {
		ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(rawAudio.length/2);
		ByteBuffer encoded = ByteBuffer.allocate(4096);
		for (int i = 0; i < rawAudio.length; i += 2) {
			int firstByte = (0x000000FF & rawAudio[i]);      //Promotes to int and handles the fact that it was unsigned.
			int secondByte = (0x000000FF & rawAudio[i+1]);  //

			//Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
			short toShort = (short) ((firstByte << 8) | secondByte);

			nonEncodedBuffer.put(toShort);
		}
		nonEncodedBuffer.flip();

		//TODO: check for 0 / negative value for error.
		int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, DiscordVoiceWS.OPUS_FRAME_SIZE, encoded, encoded.capacity());

		byte[] audio = new byte[result];
		encoded.get(audio);
		return audio;
	}

	public byte[] decodeToPCM(byte[] opusAudio) {
		ByteBuffer nonEncodedBuffer = ByteBuffer.allocate(opusAudio.length);

		ShortBuffer shortBuffer = nonEncodedBuffer.asShortBuffer();

		int result = Opus.INSTANCE.opus_decode(opusDecoder, opusAudio, opusAudio.length, shortBuffer, shortBuffer.capacity(), 0);

		nonEncodedBuffer.flip();

		byte[] audio = new byte[result];
		nonEncodedBuffer.get(audio);
		return audio;
	}
	
	public byte[] getRawAudio() {
		return rawAudio;
	}
}
