package discord4j.voice;

import discord4j.voice.json.SentSpeaking;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

class VoiceSendTask implements Runnable {

    private final ByteBuffer buf = ByteBuffer.allocate(4096);

    private final VoiceGatewayClient client;
    private final AudioProvider provider;
    private final PacketTransformer transformer;
    private final int ssrc;

    private boolean speaking = false;

    VoiceSendTask(VoiceGatewayClient client, AudioProvider provider, PacketTransformer transformer, int ssrc) {
        this.client = client;
        this.provider = provider;
        this.transformer = transformer;
        this.ssrc = ssrc;
    }

    @Override
    public void run() {
        if (provider.provide(buf)) {
            if (!speaking) {
                changeSpeaking(true);
            }

            byte[] b = new byte[buf.limit()];
            buf.get(b);
            buf.clear();

            ByteBuf packet = Unpooled.wrappedBuffer(transformer.nextSend(b));
            client.voiceSocket.send(packet);
        } else {
            if (speaking) {
                changeSpeaking(false);
            }
        }
    }

    private void changeSpeaking(boolean speaking) {
        client.send(new SentSpeaking(speaking, 0, ssrc));
        this.speaking = speaking;
    }
}
