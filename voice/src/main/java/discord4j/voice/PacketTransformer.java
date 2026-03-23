/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.voice;

import discord4j.voice.crypto.Aes256GcmEncryptionAdapter;
import discord4j.voice.crypto.EncryptionAdapter;
import discord4j.voice.crypto.EncryptionMode;
import discord4j.voice.crypto.Xchacha20Poly1305EncryptionAdapter;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.function.Function;

final class PacketTransformer {

    private static final Logger LOGGER = Loggers.getLogger(PacketTransformer.class);

    private static final int RTP_HEADER_LENGTH = 12;
    private static final int TRANSPORT_NONCE_LENGTH = 4;
    private static final int OPUS_PAYLOAD_TYPE = 0x78;
    private static final int RTP_PAYLOAD_TYPE_MASK = 0x7F;
    private static final int RTCP_PACKET_TYPE_MIN = 200;
    private static final int RTCP_PACKET_TYPE_MAX = 204;

    private final int ssrc;
    private final EncryptionAdapter encryptionAdapter;
    private final DaveProtocolSession daveSession;
    private final Function<Integer, Long> userIdLookup;

    private char seq = 0;
    private int encryptCounter;

    PacketTransformer(int ssrc, EncryptionMode encryptionMode, byte[] secretKey,
                      DaveProtocolSession daveSession, Function<Integer, Long> userIdLookup)
            throws GeneralSecurityException {
        this.ssrc = ssrc;
        this.daveSession = daveSession;
        this.userIdLookup = userIdLookup;

        switch (encryptionMode) {
            case AEAD_AES256_GCM:
                this.encryptionAdapter = new Aes256GcmEncryptionAdapter(secretKey);
                break;
            case AEAD_XCHACHA20_POLY1305:
                this.encryptionAdapter = new Xchacha20Poly1305EncryptionAdapter(secretKey);
                break;
            default:
                throw new IllegalArgumentException("Unknown encryption mode: " + encryptionMode);
        }

        SecureRandom random = new SecureRandom();
        this.encryptCounter = Math.abs(random.nextInt()) % 513 + 1;
    }

    byte @Nullable [] nextSend(byte[] audio) {
        byte[] daveEncrypted = daveSession.encrypt(ssrc, audio);
        if (daveEncrypted == null) {
            LOGGER.error("Failed to DAVE encrypt audio");
            return null;
        }

        byte[] header = this.getRtpHeader(this.seq++);

        byte[] nonce = new byte[this.encryptionAdapter.getNonceLength()];
        writeTransportNonce(nonce, this.encryptCounter);

        byte[] encrypted;
        try {
            encrypted = this.encryptionAdapter.encrypt(header, daveEncrypted, nonce);
        } catch (GeneralSecurityException e) {
            PacketTransformer.LOGGER.error("Failed to encrypt audio", e);
            return null;
        }

        byte[] finalPacket = new byte[header.length + encrypted.length + TRANSPORT_NONCE_LENGTH];
        System.arraycopy(header, 0, finalPacket, 0, header.length);
        System.arraycopy(encrypted, 0, finalPacket, header.length, encrypted.length);
        System.arraycopy(nonce, 0, finalPacket, header.length + encrypted.length, TRANSPORT_NONCE_LENGTH);

        this.encryptCounter++;

        return finalPacket;
    }

    byte @Nullable [] nextReceive(ByteBuf packet) {
        try {
            if (packet.readableBytes() < RTP_HEADER_LENGTH) {
                LOGGER.debug("Dropping truncated voice packet with {} bytes", packet.readableBytes());
                return null;
            }

            byte extensionByte = packet.readByte();
            boolean hasExtension = (extensionByte & 0x10) != 0;
            int crscLen = extensionByte & 0x0F;

            byte type = packet.readByte();
            int packetType = type & 0xFF;
            if (isRtcpPacketType(packetType)) {
                LOGGER.debug("Dropping RTCP packet type {}", packetType);
                return null;
            }
            if ((packetType & RTP_PAYLOAD_TYPE_MASK) != OPUS_PAYLOAD_TYPE) {
                LOGGER.debug("Dropping unsupported RTP payload type {}", packetType);
                return null;
            }

            packet.readChar();
            packet.readInt();
            int packetSsrc = packet.readInt();

            for (int i = 0; i < crscLen; i++) {
                packet.readInt();
            }

            short extensionLength;
            if (hasExtension) {
                packet.skipBytes(2);
                extensionLength = packet.readShort();
            } else {
                extensionLength = 0;
            }

            int headerLength = packet.readerIndex();

            byte[] header = new byte[headerLength];
            packet.getBytes(0, header);

            int readable = packet.readableBytes();
            if (readable < TRANSPORT_NONCE_LENGTH) {
                LOGGER.error("Failed to decrypt audio: truncated transport nonce");
                return null;
            }

            int encryptedLength = readable - TRANSPORT_NONCE_LENGTH;
            byte[] encrypted = new byte[encryptedLength];
            packet.getBytes(headerLength, encrypted);

            byte[] nonce = new byte[this.encryptionAdapter.getNonceLength()];
            packet.getBytes(headerLength + encryptedLength, nonce, 0, TRANSPORT_NONCE_LENGTH);

            byte[] decrypted = this.encryptionAdapter.decrypt(header, encrypted, nonce);
            int offset = 4 * extensionLength;
            if (offset > decrypted.length) {
                LOGGER.error("Failed to decrypt audio: RTP extension length overflow");
                return null;
            }

            byte[] payload = new byte[decrypted.length - offset];
            System.arraycopy(decrypted, offset, payload, 0, payload.length);

            Long userId = userIdLookup.apply(packetSsrc);
            if (userId == null) {
                LOGGER.debug("Dropping audio for SSRC {} before user mapping was announced", packetSsrc);
                return null;
            }

            byte[] daveDecrypted = daveSession.decrypt(userId.longValue(), payload);
            if (daveDecrypted == null) {
                LOGGER.error("Failed to DAVE decrypt audio for user {}", Long.toUnsignedString(userId.longValue()));
                return null;
            }

            byte[] newPacket = new byte[headerLength + daveDecrypted.length];
            System.arraycopy(header, 0, newPacket, 0, headerLength);
            System.arraycopy(daveDecrypted, 0, newPacket, headerLength, daveDecrypted.length);

            return newPacket;
        } catch (GeneralSecurityException e) {
            LOGGER.error("Failed to decrypt audio", e);
            return null;
        } finally {
            packet.release();
        }
    }

    private static void writeTransportNonce(byte[] nonce, int counter) {
        nonce[0] = (byte) ((counter >>> 24) & 0xFF);
        nonce[1] = (byte) ((counter >>> 16) & 0xFF);
        nonce[2] = (byte) ((counter >>> 8) & 0xFF);
        nonce[3] = (byte) (counter & 0xFF);
    }

    private static boolean isRtcpPacketType(int packetType) {
        return packetType >= RTCP_PACKET_TYPE_MIN && packetType <= RTCP_PACKET_TYPE_MAX;
    }

    private byte[] getRtpHeader(char seq) {
        return ByteBuffer.allocate(PacketTransformer.RTP_HEADER_LENGTH)
                .put((byte) 0x80)
                .put((byte) 0x78)
                .putChar(seq)
                .putInt(seq * Opus.FRAME_SIZE)
                .putInt(this.ssrc)
                .array();
    }

}
