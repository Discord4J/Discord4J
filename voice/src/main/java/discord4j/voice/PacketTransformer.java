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
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

final class PacketTransformer {

    private static final Logger LOGGER = Loggers.getLogger(PacketTransformer.class);

    private static final int RTP_HEADER_LENGTH = 12;

    private final int ssrc;
    private final EncryptionAdapter encryptionAdapter;

    private char seq = 0;
    private int encryptCounter;

    PacketTransformer(int ssrc, EncryptionMode encryptionMode, byte[] secretKey) throws GeneralSecurityException {
        this.ssrc = ssrc;

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

    @Nullable
    byte[] nextSend(byte[] audio) {
        byte[] header = this.getRtpHeader(this.seq++);

        byte[] nonce = new byte[this.encryptionAdapter.getNonceLength()];
        nonce[0] = (byte) ((this.encryptCounter >>> 24) & 0xFF);
        nonce[1] = (byte) ((this.encryptCounter >>> 16) & 0xFF);
        nonce[2] = (byte) ((this.encryptCounter >>> 8) & 0xFF);
        nonce[3] = (byte) (this.encryptCounter & 0xFF);

        byte[] encrypted;
        try {
            encrypted = this.encryptionAdapter.encrypt(header, audio, nonce);
        } catch (GeneralSecurityException e) {
            PacketTransformer.LOGGER.error("Failed to encrypt audio", e);
            return null;
        }

        byte[] finalPacket = new byte[header.length + encrypted.length + 4];
        System.arraycopy(header, 0, finalPacket, 0, header.length);
        System.arraycopy(encrypted, 0, finalPacket, header.length, encrypted.length);
        System.arraycopy(nonce, 0, finalPacket, header.length + encrypted.length, 4);

        this.encryptCounter++;

        return finalPacket;
    }

    @Nullable
    byte[] nextReceive(ByteBuf packet) {
        // Read header
        byte extensionByte = packet.readByte();
        boolean hasExtension = (extensionByte & 0x10) != 0;
        int crscLen = extensionByte & 0x0F;

        byte type = packet.readByte();
        char seq = packet.readChar();
        int timestamp = packet.readInt();
        int ssrc = packet.readInt();

        int[] csrc = new int[crscLen];
        for (int i = 0; i < csrc.length; i++) {
            csrc[i] = packet.readInt();
        }

        short extension;
        if (hasExtension) {
            packet.skipBytes(2);
            extension = packet.readShort();
        } else {
            extension = 0;
        }

        int headerLenght = packet.readerIndex();

        if (type != (byte) 0x78) {
            throw new IllegalArgumentException("Unsupported packet type: " + type);
        }

        byte[] header = new byte[headerLenght];
        packet.getBytes(0, header);

        int nonceLenght = this.encryptionAdapter.getNonceLength();
        int audioOffset = packet.readableBytes();
        int audioLength = audioOffset - nonceLenght;

        byte[] encrypted = new byte[audioLength];
        packet.getBytes(headerLenght, encrypted, 0, audioLength);

        byte[] nonce = new byte[nonceLenght];
        packet.getBytes(headerLenght + audioLength, nonce);

        packet.release();

        byte[] decrypted;
        try {
            decrypted = this.encryptionAdapter.decrypt(header, encrypted, nonce);
        } catch (GeneralSecurityException e) {
            PacketTransformer.LOGGER.error("Failed to decrypt audio", e);
            return null;
        }

        int offset = 4 * extension;

        byte[] newPacket = new byte[headerLenght + decrypted.length - offset];
        System.arraycopy(header, 0, newPacket, 0, headerLenght);
        System.arraycopy(decrypted, offset, newPacket, headerLenght, decrypted.length - offset);

        return newPacket;
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
