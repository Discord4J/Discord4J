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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A send task that can derive packets from an {@link AudioProvider} and submit audio packets locally.
 */
public class LocalVoiceSendTask implements Disposable {

    private final Consumer<Boolean> speakingSender;
    private final Consumer<ByteBuf> voiceSender;
    private final AudioProvider provider;
    private final PacketTransformer transformer;
    private final Disposable task;
    private final AtomicBoolean speaking = new AtomicBoolean();
    private final AtomicBoolean sentSilence = new AtomicBoolean();
    private final byte[] silence = new byte[]{(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};

    public LocalVoiceSendTask(Scheduler scheduler, Consumer<Boolean> speakingSender, Consumer<ByteBuf> voiceSender,
                              AudioProvider provider, PacketTransformer transformer) {
        this.speakingSender = speakingSender;
        this.voiceSender = voiceSender;
        this.provider = provider;
        this.transformer = transformer;
        this.task = scheduler.schedulePeriodically(this::run, 0, Opus.FRAME_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        if (speaking.compareAndSet(true, false)) {
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
            if (speaking.compareAndSet(false, true)) {
                changeSpeaking(true);
            }

            byte[] b = new byte[provider.getBuffer().limit()];
            provider.getBuffer().get(b);
            provider.getBuffer().clear();
            ByteBuf packet = Unpooled.wrappedBuffer(transformer.nextSend(b));

            voiceSender.accept(packet);
        } else if (speaking.compareAndSet(true, false)) {
            changeSpeaking(false);
        } else if (sentSilence.compareAndSet(false, true)) {
            voiceSender.accept(Unpooled.wrappedBuffer(transformer.nextSend(silence)));
        }
    }

    private void changeSpeaking(boolean speaking) {
        speakingSender.accept(speaking);
    }
}
