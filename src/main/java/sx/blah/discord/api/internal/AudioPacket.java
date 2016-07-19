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

import org.peergos.crypto.TweetNaCl;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents the contents of a audio packet that was either received from Discord or will be sent to discord.
 */
public class AudioPacket {

	private static final int RTP_HEADER_LENGTH = 12;
	private static final byte RTP_TYPE = (byte) 0x80;
	private static final byte RTP_VERSION = (byte) 0x78;

	private static final int RTP_TYPE_LOC = 0;
	private static final int RTP_VERSION_LOC = 1;
	private static final int RTP_SEQ_LOC = 2;
	private static final int RTP_TIMESTAMP_LOC = 4;
	private static final int RTP_SSRC_LOC = 8;

	private final char seq;
	private final int timestamp;
	private final int ssrc; // Unique per user if we're receiving. Constant for sending.
	private final byte[] encodedAudio; // An AudioPacket will only ever contain opus encoded audio. Result of OpusUtil.decodeToPCM() should be stored elsewhere.
	private final byte[] rawPacket;

	/**
	 * Instantiate a new packet using information received by Discord in a UDP packet. (Receiving)
	 * @param packet The received packet.
	 * @return The new AudioPacket.
	 */
	static AudioPacket fromUdpPacket(DatagramPacket packet) {
		return new AudioPacket(packet);
	}

	/**
	 * Instantiate a new packet using information provided by us. (Sending)
	 * @param seq The sequence number of this packet. Kept track of in {@link DiscordVoiceWS#setupSendThread()}.
	 * @param timestamp The timestamp of this packet.
	 * @param ssrc The synchronization source identifier. This *should* be the same for every packet sent on the same WS.
	 * @param encodedAudio The opus-encoded audio being sent in this packet.
	 * @return The new AudioPacket.
	 */
	static AudioPacket fromEncodedAudio(char seq, int timestamp, int ssrc, byte[] encodedAudio) {
		return new AudioPacket(seq, timestamp, ssrc, encodedAudio);
	}

	private AudioPacket(DatagramPacket packet) {
		this(Arrays.copyOf(packet.getData(), packet.getLength()));
	}

	private AudioPacket(byte[] rawPacket) {
		this.rawPacket = rawPacket;

		ByteBuffer buffer = ByteBuffer.wrap(rawPacket);
		this.seq = buffer.getChar(RTP_SEQ_LOC);
		this.timestamp = buffer.getInt(RTP_TIMESTAMP_LOC);
		this.ssrc = buffer.getInt(RTP_SSRC_LOC);

		byte[] audio = new byte[buffer.array().length - RTP_HEADER_LENGTH];
		System.arraycopy(buffer.array(), RTP_HEADER_LENGTH, audio, 0, audio.length);
		this.encodedAudio = audio;
	}

	private AudioPacket(char seq, int timestamp, int ssrc, byte[] encodedAudio) {
		this.seq = seq;
		this.ssrc = ssrc;
		this.timestamp = timestamp;
		this.encodedAudio = encodedAudio;

		ByteBuffer buffer = ByteBuffer.allocate(12 + encodedAudio.length);
		buffer.put    (RTP_TYPE_LOC,       RTP_TYPE);
		buffer.put    (RTP_VERSION_LOC,    RTP_VERSION);
		buffer.putChar(RTP_SEQ_LOC,        seq);
		buffer.putInt (RTP_TIMESTAMP_LOC,  timestamp);
		buffer.putInt (RTP_SSRC_LOC,       ssrc);
		System.arraycopy(encodedAudio, 0, buffer.array(), RTP_HEADER_LENGTH, encodedAudio.length);
		this.rawPacket = buffer.array();
	}

	/**
	 * Encrypts the packet using TweetNaCl.
	 * @param secret The secret key sent by Discord.
	 * @return The encrypted version of the packet.
	 */
	AudioPacket encrypt(byte[] secret) {
		byte[] encryptionNonce = getEncryptionNonce();
		byte[] encryptedAudio = TweetNaCl.secretbox(encodedAudio, encryptionNonce, secret);

		return new AudioPacket(seq, timestamp, ssrc, encryptedAudio);
	}

	/**
	 * Decrypts the packet using TweetNaCl.
	 * @param secret The secret key sent by Discord.
	 * @return The decrypted version of the packet.
	 */
	AudioPacket decrypt(byte[] secret) {
		byte[] encryptionNonce = getEncryptionNonce();
		byte[] header = getHeader();
		byte[] decryptedAudio = TweetNaCl.secretbox_open(encodedAudio, encryptionNonce, secret);

		byte[] decryptedPacket = new byte[RTP_HEADER_LENGTH + encodedAudio.length];
		System.arraycopy(header, 0, decryptedPacket, 0, RTP_HEADER_LENGTH);
		System.arraycopy(decryptedAudio, 0, decryptedPacket, RTP_HEADER_LENGTH, decryptedAudio.length);

		return new AudioPacket(decryptedPacket);
	}

	/**
	 * Returns a DatagramPacket suitable for sending through UDP.
	 * @param address Address to send the packet to.
	 * @return The packet.
	 */
	DatagramPacket asUdpPacket(InetSocketAddress address) {
		return new DatagramPacket(Arrays.copyOf(rawPacket, rawPacket.length), rawPacket.length, address);
	}

	/**
	 * Gets the extended nonce byte array used for encryption. This is length 24 while Discord uses length 12.
	 * The first 12 bytes of the extended array are filled and the rest are left as 0.
	 * @return The nonce used for encryption.
	 */
	private byte[] getEncryptionNonce() {
		byte[] encryptionNonce = new byte[24]; // Encryption uses 24 byte nonce while Discord uses 12
		byte[] header = getHeader();
		System.arraycopy(header, 0, encryptionNonce, 0, RTP_HEADER_LENGTH); // Copy of the header into the extended nonce. Leave the remaining bytes 0

		return encryptionNonce;
	}

	/**
	 * Gets the shorter 12 byte header used in RTP.
	 * This is the first 12 bytes of the raw packet.
	 * @return The header byte array.
	 */
	private byte[] getHeader() {
		return Arrays.copyOf(rawPacket, RTP_HEADER_LENGTH);
	}

	/**
	 * Gets the encoded audio byte array stored in this packet.
	 * Note: The encoded audio is everything after the first 12 bytes of the raw packet.
	 * @return The encoded audio
	 */
	byte[] getEncodedAudio() {
		return encodedAudio;
	}

	/**
	 * Gets the ssrc of this packet.
	 * @return The ssrc.
	 */
	int getSsrc() {
		return ssrc;
	}
}
