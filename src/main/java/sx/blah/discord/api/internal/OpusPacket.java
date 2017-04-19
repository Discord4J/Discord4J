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

import org.apache.commons.lang3.ArrayUtils;
import org.peergos.crypto.TweetNaCl;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

class OpusPacket {

	final RTPHeader header;
	private byte[] audio;

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

	void encrypt(byte[] secret) {
		if (isEncrypted) throw new IllegalStateException("Attempt to encrypt already-encrypted audio packet.");

		audio = TweetNaCl.secretbox(audio, getNonce(), secret);
		isEncrypted = true;
	}

	void decrypt(byte[] secret) {
		if (!isEncrypted) throw new IllegalStateException("Attempt to decrypt unencrypted audio packet.");

		audio = TweetNaCl.secretbox_open(audio, getNonce(), secret);
		isEncrypted = false;
	}

	byte[] asByteArray() {
		return ArrayUtils.addAll(header.asByteArray(), audio);
	}

	byte[] getAudio() {
		return ArrayUtils.clone(audio);
	}

	private byte[] getNonce() {
		return ArrayUtils.addAll(header.asByteArray(), new byte[12]);
	}

	static class RTPHeader {

		static final int LENGTH = 12;

		final byte type = (byte) 0x80;
		final byte version = (byte) 0x78;
		final char sequence;
		final int timestamp;
		final int ssrc;

		RTPHeader(byte[] header) {
			ByteBuffer buf = ByteBuffer.wrap(header);
			this.sequence = buf.getChar(2);
			this.timestamp = buf.getInt(4);
			this.ssrc = buf.getInt(8);
		}

		RTPHeader(char sequence, int timestamp, int ssrc) {
			this.sequence = sequence;
			this.timestamp = timestamp;
			this.ssrc = ssrc;
		}

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
