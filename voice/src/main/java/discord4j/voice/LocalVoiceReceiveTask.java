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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A receive task that can locally consume audio packets and pass them to an {@link AudioReceiver}.
 */
@SuppressWarnings("deprecation")
public class LocalVoiceReceiveTask implements Disposable {

    private static final Logger log = Loggers.getLogger(LocalVoiceReceiveTask.class);

    private final Disposable task;

    public LocalVoiceReceiveTask(Scheduler scheduler, Flux<ByteBuf> in, PacketTransformer transformer,
                                 AudioReceiver receiver) {
        this.task = in
                .flatMap(packet -> Mono.fromCallable(() -> transformer.nextReceive(packet))
                        .map(buf -> {
                            if (receiver != AudioReceiver.NO_OP) {
                                receiver.getBuffer().put(buf);
                                receiver.getBuffer().flip();
                                receiver.receive();
                            }
                            return buf;
                        })
                        .onErrorResume(t -> {
                            log.error("Error while receiving audio", t);
                            return Mono.empty();
                        }))
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void dispose() {
        task.dispose();
    }

    @Override
    public boolean isDisposed() {
        return task.isDisposed();
    }
}
