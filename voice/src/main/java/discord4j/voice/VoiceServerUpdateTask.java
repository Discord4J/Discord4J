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

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A listener to track voice server changes due to migrations or disconnections. A voice client can then react to
 * external changes coming from the gateway.
 */
@FunctionalInterface
public interface VoiceServerUpdateTask {

    /**
     * Return a Mono sequence containing an eventual {@link VoiceServerOptions} instance indicating a voice server
     * update payload was received from the gateway connection for the given guild.
     *
     * @param guildId the guild ID listening for voice server updates
     * @return a Mono with a VoiceServerOptions payload
     * @deprecated for removal in future versions, migrate to {@link #onVoiceServerUpdates(Snowflake)} as voice clients
     * can receive multiple voice server updates throughout their lifecycle
     */
    @Deprecated
    Mono<VoiceServerOptions> onVoiceServerUpdate(Snowflake guildId);

    /**
     * Return a Flux sequence containing {@link VoiceServerOptions} instances indicating a voice server update payload
     * was received from the gateway connection for the given guild.
     *
     * @param guildId the guild ID listening for voice server updates
     * @return a Flux with a VoiceServerOptions payload
     */
    default Flux<VoiceServerOptions> onVoiceServerUpdates(Snowflake guildId) {
        return onVoiceServerUpdate(guildId).repeat();
    }

}
