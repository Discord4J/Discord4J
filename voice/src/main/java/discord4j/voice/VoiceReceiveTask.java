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
import reactor.util.Logger;
import reactor.util.Loggers;

class VoiceReceiveTask implements Disposable {

    private static final Logger log = Loggers.getLogger(VoiceReceiveTask.class);

    private final Disposable task;

    VoiceReceiveTask(Flux<ByteBuf> in, PacketTransformer transformer, AudioReceiver receiver) {
        this.task = in
                .flatMap(packet -> Mono.justOrEmpty(transformer.nextReceive(packet)))
                .map(buf -> {
                    if (receiver != AudioReceiver.NO_OP) {
                        receiver.getBuffer().put(buf);
                        receiver.getBuffer().flip();
                        receiver.receive();
                    }
                    return buf;
                })
                .onErrorContinue((t, o) -> log.error("Error while receiving audio", t))
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
