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

import discord4j.common.annotations.Experimental;
import reactor.core.publisher.Mono;

/**
 * A factory to create {@link VoiceConnection} instances using a set of {@link VoiceGatewayOptions}.
 */
@Experimental
public interface VoiceConnectionFactory {

    /**
     * Return a {@link Mono} that, upon subscription, is able to obtain a {@link VoiceConnection} from the given
     * {@link VoiceGatewayOptions}. The resulting connection can be an existing one if it's still active for a guild.
     *
     * @param options the set of options to configure voice connections
     * @return a {@link Mono} that, upon subscription, can return a {@link VoiceConnection}. In case an error occurs,
     * it is emitted through the {@link Mono}.
     */
    Mono<VoiceConnection> create(VoiceGatewayOptions options);
}
