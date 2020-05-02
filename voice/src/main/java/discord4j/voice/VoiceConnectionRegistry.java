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
 * A centralized registry to hold {@link VoiceConnection} instances.
 */
@Experimental
public interface VoiceConnectionRegistry {

    /**
     * Return the current {@link VoiceConnection} this registry holds for a given {@code guildId}.
     *
     * @param guildId the guild ID to fetch the current voice connection
     * @return a {@link Mono} of {@link VoiceConnection} for the given guild if present, empty otherwise.
     */
    Mono<VoiceConnection> getVoiceConnection(long guildId);

    /**
     * Register a {@link VoiceConnection} for a given {@code guildId}, replacing any existing one.
     *
     * @param guildId the guild ID to set the new voice connection
     * @param voiceConnection the {@link VoiceConnection} to register
     * @return a {@link Mono} indicating completion of the registration process, if an error happens it is emitted
     * through the {@link Mono}.
     */
    Mono<Void> registerVoiceConnection(long guildId, VoiceConnection voiceConnection);

    /**
     * Disconnect a {@link VoiceConnection} for given {@code guildId} and remove it from the registry. If no connection
     * was present for the guild, this method does nothing.
     *
     * @param guildId the guild ID to disconnect and remove a voice connection
     * @return a {@link Mono} indicating completion of the disconnection process, if an error happens it is emitted
     * through the {@link Mono}.
     */
    Mono<Void> disconnect(long guildId);
}
