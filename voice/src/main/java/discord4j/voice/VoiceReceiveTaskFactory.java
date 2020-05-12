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
 * A factory to create a task that receives audio packets from a source and processes them through a given
 * {@link AudioReceiver}.
 */
public interface VoiceReceiveTaskFactory {

    /**
     * Create a task that is capable of handling incoming audio packets.
     *
     * @param scheduler a dedicated {@link Scheduler} that can be used to run the task
     * @param in a sequence of raw incoming audio {@link ByteBuf} packets
     * @param transformer a strategy to decode a packet from a raw {@link ByteBuf}
     * @param receiver a strategy to consume decoded audio packets
     * @return a task that can receive audio and process it
     */
    Disposable create(Scheduler scheduler, Flux<ByteBuf> in, PacketTransformer transformer, AudioReceiver receiver);

}
