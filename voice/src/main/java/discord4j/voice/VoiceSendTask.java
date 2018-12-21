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

import discord4j.voice.json.SentSpeaking;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

class VoiceSendTask implements Disposable {

    private final VoiceGatewayClient client;
    private final AudioProvider provider;
    private final PacketTransformer transformer;
    private final int ssrc;
    private final Disposable task;

    private boolean speaking = false;

    VoiceSendTask(Scheduler scheduler, VoiceGatewayClient client, AudioProvider provider, PacketTransformer transformer, int ssrc) {
        this.client = client;
        this.provider = provider;
        this.transformer = transformer;
        this.ssrc = ssrc;
        this.task = scheduler.schedulePeriodically(this::run, 0, Opus.FRAME_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        if (speaking) {
            changeSpeaking(false);
        }
        task.dispose();
    }

    @Override
    public boolean isDisposed() {
        return task.isDisposed();
    }

    private void run() {
        if (provider.provide()) {
            if (!speaking) {
                changeSpeaking(true);
            }

            byte[] b = new byte[provider.getBuffer().limit()];
            provider.getBuffer().get(b);
            provider.getBuffer().clear();
            ByteBuf packet = Unpooled.wrappedBuffer(transformer.nextSend(b));

            client.voiceSocket.send(packet);
        } else if (speaking) {
            changeSpeaking(false);
        }
    }

    private void changeSpeaking(boolean speaking) {
        client.send(new SentSpeaking(speaking, 0, ssrc));
        this.speaking = speaking;
    }
}
