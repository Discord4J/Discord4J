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

import com.iwebpp.crypto.TweetNaclFast;
import org.apache.commons.lang3.ArrayUtils;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Stores information describing an audio packet sent or received from Discord on a {@link UDPVoiceSocket}.
 */
class OpusPacket {

	/**
	 * The RTP header of the packet.
	 */
	final RTPHeader header;
	/**
	 * The audio data in the packet. This data can be either encrypted or decrypted depending on {@link #isEncrypted}.
	 */
	private byte[] audio;

	/**
	 * Whether the data in {@link #audio} is encrypted.
	 */
	private boolean isEncrypted = false;

	OpusPacket(DatagramPacket udpPacket) {
		this.header = new RTPHeader(Arrays.copyOf(udpPacket.getData(), RTPHeader.LENGTH));
		this.audio = Arrays.copyOfRange(udpPacket.getData(), RTPHeader.LENGTH, udpPacket.getLength());
		this.isEncrypted = true;
	}

	OpusPacket(char sequence, int timestamp, int ssrc, byte[] audio) {
		this(new RTPHeader(sequence, timestamp, ssrc), audio);
	}

	private OpusPacket(RTPHeader header, byte[] audio) {
		this.header = header;
		this.audio = audio;
	}

	/**
	 * Encrypts the data in {@link #audio} with the given key using {@link TweetNaclFast.Box#box(byte[], byte[])}.
	 *
	 * @param secret The secret key to use in encryption.
	 */
	void encrypt(byte[] secret) {
		if (isEncrypted) throw new IllegalStateException("Attempt to encrypt already-encrypted audio packet.");
		audio = new TweetNaclFast.SecretBox(secret).box(audio, getNonce());
		isEncrypted = true;
	}

	/**
	 * Decrypts the data in {@link #audio} with the given key using {@link TweetNaclFast.Box#open(byte[], byte[])}.
	 *
	 * @param secret The secret key to use in decryption.
	 */
	void decrypt(byte[] secret) {
		if (!isEncrypted) throw new IllegalStateException("Attempt to decrypt unencrypted audio packet.");
		audio = new TweetNaclFast.SecretBox(secret).open(audio, getNonce());
		isEncrypted = false;

		if (header.type == (byte) 0x90 && audio[0] == (byte) 0xBE && audio[1] == (byte) 0xDE) {
			int hlen = audio[2] << 8 | audio[3];
			int i = 4;
			for (; i < hlen + 4; i++)
			{
				int b = audio[i];
				int len = (b & 0x0F) + 1;
				i += len;
			}
			while (audio[i] == 0)
				i++;

			byte[] buf = new byte[audio.length - i];
			System.arraycopy(audio, i, buf, 0, buf.length);
			audio = buf;
		}
	}

	/**
	 * Gets the packet as a byte array. The array first contains the {@link #header} an then the audio data.
	 *
	 * @return The packet as a byte array.
	 */
	byte[] asByteArray() {
		return ArrayUtils.addAll(header.asByteArray(), audio);
	}

	/**
	 * Gets a copy of the audio data in the packet.
	 *
	 * @return A copy of the audio data in the packet.
	 */
	byte[] getAudio() {
		return ArrayUtils.clone(audio);
	}

	/**
	 * Gets the nonce used for encryption. This is the {@link #header} as a byte array with 12 0s appended to the end.
	 *
	 * @return The nonce used for encryption.
	 */
	private byte[] getNonce() {
		return ArrayUtils.addAll(header.asByteArray(), new byte[12]);
	}

	/**
	 * Contains information about an audio packet excluding the actual audio.
	 * @see <a href="https://tools.ietf.org/html/rfc3550">https://tools.ietf.org/html/rfc3550</a>
	 */
	static class RTPHeader {

		/**
		 * The length of the header in bytes.
		 */
		static final int LENGTH = 12;

		/**
		 * The type of the packet.
		 */
		final byte type;
		/**
		 * The version of the packet.
		 */
		final byte version;
		/**
		 * Incremented for each packet received on the socket.
		 */
		final char sequence;
		/**
		 * Incremented for each packet received on the socket.
		 */
		final int timestamp;
		/**
		 * Unique number used to identify the user speaking.
		 */
		final int ssrc;

		RTPHeader(byte[] header) {
			ByteBuffer buf = ByteBuffer.wrap(header);
			this.type = buf.get(0);
			this.version = buf.get(1);
			this.sequence = buf.getChar(2);
			this.timestamp = buf.getInt(4);
			this.ssrc = buf.getInt(8);
		}

		RTPHeader(char sequence, int timestamp, int ssrc) {
			this.type = (byte) 0x80;
			this.version = 0x78;
			this.sequence = sequence;
			this.timestamp = timestamp;
			this.ssrc = ssrc;
		}

		/**
		 * Gets the header as a byte array of length {@link #LENGTH}.
		 *
		 * @return The header as a byte array.
		 */
		byte[] asByteArray() {
			return ByteBuffer.allocate(LENGTH)
					.put(0, type)
					.put(1, version)
					.putChar(2, sequence)
					.putInt(4, timestamp)
					.putInt(8, ssrc)
					.array();
		}
	}
}
