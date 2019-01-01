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

import com.iwebpp.crypto.TweetNaclFast;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

final class PacketTransformer {

    private static final int RTP_HEADER_LENGTH = 12;
    private static final int EXTENDED_RTP_HEADER_LENGTH = 24;

    private final int ssrc;
    private final TweetNaclFast.SecretBox boxer;

    private char seq = 0;

    PacketTransformer(int ssrc, TweetNaclFast.SecretBox boxer) {
        this.ssrc = ssrc;
        this.boxer = boxer;
    }

    byte[] nextSend(byte[] audio) {
        byte[] header = getRtpHeader(seq++);
        byte[] encrypted = boxer.box(audio, getNonce(header));

        return getAudioPacket(header, encrypted);
    }

    @Nullable
    byte[] nextReceive(ByteBuf packet) {
        byte[] header = new byte[RTP_HEADER_LENGTH];
        packet.getBytes(0, header);

        int audioOffset = RTP_HEADER_LENGTH + (4 * (byte) (header[0] & 0x0F));

        byte[] encrypted = new byte[packet.readableBytes() - audioOffset];
        packet.getBytes(audioOffset, encrypted);

        byte[] decrypted = boxer.open(encrypted, getNonce(header));
        if (decrypted == null) {
            return null;
        }

        byte[] newPacket = new byte[RTP_HEADER_LENGTH + decrypted.length];
        System.arraycopy(header, 0, newPacket, 0, RTP_HEADER_LENGTH);
        System.arraycopy(decrypted, 0, newPacket, audioOffset, decrypted.length);
        return newPacket;
    }

    private byte[] getNonce(byte[] rtpHeader) {
        byte[] nonce = new byte[EXTENDED_RTP_HEADER_LENGTH];
        System.arraycopy(rtpHeader, 0, nonce, 0, RTP_HEADER_LENGTH);
        return nonce;
    }

    private byte[] getRtpHeader(char seq) {
        return ByteBuffer.allocate(RTP_HEADER_LENGTH)
                .put((byte) 0x80)
                .put((byte) 0x78)
                .putChar(seq)
                .putInt(seq * Opus.FRAME_SIZE)
                .putInt(ssrc)
                .array();
    }

    private static byte[] getAudioPacket(byte[] rtpHeader, byte[] encryptedAudio) {
        byte[] packet = new byte[rtpHeader.length + encryptedAudio.length];
        System.arraycopy(rtpHeader, 0, packet, 0, rtpHeader.length);
        System.arraycopy(encryptedAudio, 0, packet, rtpHeader.length, encryptedAudio.length);
        return packet;
    }

}
