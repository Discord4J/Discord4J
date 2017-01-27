package sx.blah.discord.api.internal;

import org.apache.commons.lang3.ArrayUtils;
import org.peergos.crypto.TweetNaCl;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class OpusPacket {

	private RTPHeader header;
	private byte[] audio;

	private boolean isEncrypted = false;

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

	private byte[] getNonce() {
		return ArrayUtils.addAll(header.asByteArray(), new byte[12]);
	}

	static class RTPHeader {

		static final int LENGTH = 12;

		private byte type = (byte) 0x80;
		private byte version = (byte) 0x78;
		private char sequence;
		private int timestamp;
		private int ssrc;

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
