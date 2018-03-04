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
package discord4j.core.object;

import discord4j.common.json.response.VoiceStateResponse;
import discord4j.core.Client;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A Discord voice state.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/voice#voice-resource">Voice Resource</a>
 */
public final class VoiceState implements DiscordObject {

	/** The Client associated to this object. */
	private final Client client;

	/** The raw data as represented by Discord. */
	private final VoiceStateResponse voiceState;

	/**
	 * Constructs a {@code VoiceState} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param voiceState The raw data as represented by Discord, must be non-null.
	 */
	public VoiceState(final Client client, final VoiceStateResponse voiceState) {
		this.client = Objects.requireNonNull(client);
		this.voiceState = Objects.requireNonNull(voiceState);
	}

	@Override
	public Client getClient() {
		return client;
	}

	/**
	 * Gets the guild ID this voice state is for.
	 *
	 * @return The guild ID this voice state is for.
	 */
	public Snowflake getGuildId() {
		return Snowflake.of(voiceState.getGuildId());
	}

	/**
	 * Requests to retrieve the guild this voice state is for.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this voice state is for. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<Guild> getGuild() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the channel ID this user is connected to, if present.
	 *
	 * @return The channel ID this user is connected to, if present.
	 */
	public Optional<Snowflake> getChannelId() {
		return Optional.ofNullable(voiceState.getChannelId()).map(Snowflake::of);
	}

	/**
	 * Requests to retrieve the channel this user is connected to, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} this user is connected
	 * to, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<VoiceChannel> getChannel() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the user ID this voice state is for.
	 *
	 * @return The user ID this voice state is for.
	 */
	public Snowflake getUserId() {
		return Snowflake.of(voiceState.getUserId());
	}

	/**
	 * Requests to retrieve the user this voice state is for.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User} this voice state is for. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<User> getUser() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the session ID for this voice state.
	 *
	 * @return The session ID for this voice state.
	 */
	public String getSessionId() {
		return voiceState.getSessionId();
	}

	/**
	 * Gets whether this user is deafened by the server.
	 *
	 * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
	 */
	public boolean isDeaf() {
		return voiceState.isDeaf();
	}

	/**
	 * Gets whether this user is muted by the server.
	 *
	 * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
	 */
	public boolean isMuted() {
		return voiceState.isMute();
	}

	/**
	 * Gets whether this user is locally deafened.
	 *
	 * @return {@code true} if this user is locally deafened, {@code false} otherwise.
	 */
	public boolean isSelfDeaf() {
		return voiceState.isSelfDeaf();
	}

	/**
	 * Gets whether this user is locally muted.
	 *
	 * @return {@code true} if this user is locally muted, {@code false} otherwise.
	 */
	public boolean isSelfMuted() {
		return voiceState.isSelfMute();
	}

	/**
	 * Gets whether this user is muted by the current user.
	 *
	 * @return {@code true} if this user is muted by the current user, {@code false} otherwise.
	 */
	public boolean isSuppressed() {
		return voiceState.isSuppress();
	}
}
