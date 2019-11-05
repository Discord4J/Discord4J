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

package discord4j.gateway;

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * A transformation function to a sequence of raw {@link ByteBuf} payloads.
 */
@FunctionalInterface
public interface PayloadTransformer {

    /**
     * Transform a sequence of {@link ByteBuf} payloads, along with their parent {@link GatewayClient}, to inject
     * behavior like delays into the produced sequence of {@link ByteBuf} payloads.
     *
     * @param sequence a sequence of payloads
     * @return the transformed sequence
     */
    Publisher<ByteBuf> apply(Flux<Tuple2<GatewayClient, ByteBuf>> sequence);
}
