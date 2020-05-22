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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.voice;

import io.netty.buffer.ByteBuf;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

/**
 * A local implementation of a {@link VoiceReceiveTaskFactory}, performing all actions in the same JVM instance.
 */
public class LocalVoiceReceiveTaskFactory implements VoiceReceiveTaskFactory {

    @Override
    public Disposable create(Scheduler scheduler, Flux<ByteBuf> in, PacketTransformer transformer,
                             AudioReceiver receiver) {
        return new LocalVoiceReceiveTask(scheduler, in, transformer, receiver);
    }
}
