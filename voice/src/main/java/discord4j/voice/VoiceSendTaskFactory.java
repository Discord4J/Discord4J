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
import reactor.core.scheduler.Scheduler;

import java.util.function.Consumer;

/**
 * A factory to create a task that reads audio packets from an {@link AudioProvider}, encodes them and then sends them
 * through a given raw packet sink.
 */
public interface VoiceSendTaskFactory {

    /**
     * Create a task that is capable of handling outbound audio packets.
     *
     * @param scheduler a dedicated {@link Scheduler} that can be used to run the task
     * @param speakingSender a sink capable to signaling speaking status to Discord
     * @param voiceSender a sink capable of sending outbound audio to Discord
     * @param provider a strategy to produce audio packets that can be encoded
     * @param transformer a strategy to encode a packet into a raw buffer
     * @return a task that can process audio and send it
     */
    Disposable create(Scheduler scheduler, Consumer<Boolean> speakingSender, Consumer<ByteBuf> voiceSender,
                      AudioProvider provider, PacketTransformer transformer);

}
