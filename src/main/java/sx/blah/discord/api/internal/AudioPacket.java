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
package sx.blah.discord.api.internal;

import com.sun.jna.ptr.PointerByReference;
import org.peergos.crypto.TweetNaCl;
import sx.blah.discord.handle.audio.impl.AudioManager;

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

	private static PointerByReference stereoOpusEncoder;
	private static PointerByReference monoOpusEncoder;
	private static PointerByReference stereoOpusDecoder;
	private static PointerByReference monoOpusDecoder;

	static {
		try {
			IntBuffer error = IntBuffer.allocate(4);
			stereoOpusDecoder = Opus.INSTANCE.opus_decoder_create(AudioManager.OPUS_SAMPLE_RATE, AudioManager.OPUS_STEREO_CHANNEL_COUNT, error);

			error = IntBuffer.allocate(4);
			monoOpusDecoder = Opus.INSTANCE.opus_decoder_create(AudioManager.OPUS_SAMPLE_RATE, AudioManager.OPUS_MONO_CHANNEL_COUNT, error);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			stereoOpusEncoder = null;
			stereoOpusDecoder = null;
			monoOpusEncoder = null;
			monoOpusDecoder = null;
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

	private AudioPacket(byte[] rawPacket) { //FIXME: Support mono & decryption
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

	AudioPacket(char seq, int timestamp, int ssrc, byte[] rawAudio, byte[] secret) {
		this.seq = seq;
		this.ssrc = ssrc;
		this.timestamp = timestamp;
		this.rawAudio = rawAudio;

		ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
		nonceBuffer.put(0, (byte) 0x80);
		nonceBuffer.put(1, (byte) 0x78);
		nonceBuffer.putChar(2, seq);
		nonceBuffer.putInt(4, timestamp);
		nonceBuffer.putInt(8, ssrc);
		this.encodedAudio = TweetNaCl.secretbox(rawAudio,
				Arrays.copyOf(nonceBuffer.array(), 24), //encryption nonce is 24 bytes long while discord's is 12 bytes long
				secret);

		byte[] packet = new byte[nonceBuffer.capacity()+encodedAudio.length];
		System.arraycopy(nonceBuffer.array(), 0, packet, 0, 12); //Add nonce
		System.arraycopy(encodedAudio, 0, packet, 12, encodedAudio.length); //Add audio

		this.rawPacket = packet;
	}

	private byte[] getRawPacket() {
		return Arrays.copyOf(rawPacket, rawPacket.length);
	}

	DatagramPacket asUdpPacket(InetSocketAddress address) {
		return new DatagramPacket(getRawPacket(), rawPacket.length, address);
	}

	private byte[] decodeToPCM(byte[] opusAudio) {
		ByteBuffer nonEncodedBuffer = ByteBuffer.allocate(opusAudio.length);

		ShortBuffer shortBuffer = nonEncodedBuffer.asShortBuffer();

		int result = Opus.INSTANCE.opus_decode(stereoOpusDecoder, opusAudio, opusAudio.length, shortBuffer, shortBuffer.capacity(), 0);

		nonEncodedBuffer.flip();

		byte[] audio = new byte[result];
		nonEncodedBuffer.get(audio);
		return audio;
	}

	public byte[] getRawAudio() {
		return rawAudio;
	}
}
