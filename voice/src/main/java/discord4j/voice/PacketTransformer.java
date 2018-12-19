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
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.Arrays;

final class PacketTransformer {

    private static final int OPUS_SAMPLE_RATE = 48_000;
    private static final int OPUS_FRAME_TIME = 20;
    private static final int OPUS_FRAME_SIZE = 960; //  (48000Hz) / (1s / 20ms)

    private static final int RTP_HEADER_LENGTH = 1 + 1 + 2 + 4 + 4;

//    private final Flux<byte[]> headers = Flux.<Character, Character>generate(() -> (char) 0, (c, sink) -> {
//        sink.next(c);
//        return (char) (c + 1);
//    }).map(this::getRtpHeader);

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
//        System.out.println(Arrays.toString(encrypted));

        byte[] a = getAudioPacket(header, encrypted);

        return a;
    }

//    Flux<ByteBuf> send(Flux<byte[]> in) {
//        return Flux.zip(headers, in)
//                .map(t -> {
//                    byte[] rtpHeader = t.getT1();
//                    byte[] audio = t.getT2();
//
//                    return getAudioPacket(rtpHeader, boxer.box(audio, getNonce(rtpHeader)));
//                }).map(Unpooled::wrappedBuffer);
//    }
//
//    Flux<ByteBuf> receive(Flux<ByteBuf> in) {
//        return in.map(buf -> {
//            byte[] rtpHeader = new byte[RTP_HEADER_LENGTH];
//            buf.readBytes(rtpHeader);
//
//            byte[] encryptedAudio = new byte[buf.readableBytes()];
//            buf.readBytes(encryptedAudio);
//            byte[] decryptedAudio = boxer.open(encryptedAudio, getNonce(rtpHeader));
//
//            byte[] newPacket = new byte[RTP_HEADER_LENGTH + decryptedAudio.length];
//            System.arraycopy(rtpHeader, 0, newPacket, 0, RTP_HEADER_LENGTH);
//            System.arraycopy(decryptedAudio, 0, newPacket, RTP_HEADER_LENGTH, decryptedAudio.length);
//
//            return newPacket;
//        }).map(Unpooled::wrappedBuffer);
//    }

    private byte[] getNonce(byte[] rtpHeader) {
        byte[] nonce = new byte[24];
        System.arraycopy(rtpHeader, 0, nonce, 0, RTP_HEADER_LENGTH);
        return nonce;
    }

    private byte[] getRtpHeader(char seq) {
        return ByteBuffer.allocate(RTP_HEADER_LENGTH)
                .put((byte) 0x80)
                .put((byte) 0x78)
                .putChar(seq)
                .putInt(seq * OPUS_FRAME_SIZE)
                .putInt(ssrc)
                .array();
    }

    private static byte[] getAudioPacket(byte[] rtpHeader, byte[] encryptedAudio) {
        return ByteBuffer.allocate(rtpHeader.length + encryptedAudio.length)
                .put(rtpHeader)
                .put(encryptedAudio)
                .array();
    }

}