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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.VoiceStateBean;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord voice state.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/voice#voice-resource">Voice Resource</a>
 */
public final class VoiceState implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final VoiceStateBean data;

    /**
     * Constructs a {@code VoiceState} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public VoiceState(final ServiceMediator serviceMediator, final VoiceStateBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the guild ID this voice state is for.
     *
     * @return The guild ID this voice state is for.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(data.getGuildId());
    }

    /**
     * Requests to retrieve the guild this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the channel ID this user is connected to, if present.
     *
     * @return The channel ID this user is connected to, if present.
     */
    public Optional<Snowflake> getChannelId() {
        return Optional.ofNullable(data.getChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel this user is connected to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} this user is connected
     * to, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getChannel() {
        return Mono.justOrEmpty(getChannelId()).flatMap(getClient()::getChannelById).cast(VoiceChannel.class);
    }

    /**
     * Gets the user ID this voice state is for.
     *
     * @return The user ID this voice state is for.
     */
    public Snowflake getUserId() {
        return Snowflake.of(data.getUserId());
    }

    /**
     * Requests to retrieve the user this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Requests to retrieve the member this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    /**
     * Gets the session ID for this voice state.
     *
     * @return The session ID for this voice state.
     */
    public String getSessionId() {
        return data.getSessionId();
    }

    /**
     * Gets whether this user is deafened by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isDeaf() {
        return data.isDeaf();
    }

    /**
     * Gets whether this user is muted by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isMuted() {
        return data.isMute();
    }

    /**
     * Gets whether this user is locally deafened.
     *
     * @return {@code true} if this user is locally deafened, {@code false} otherwise.
     */
    public boolean isSelfDeaf() {
        return data.isSelfDeaf();
    }

    /**
     * Gets whether this user is locally muted.
     *
     * @return {@code true} if this user is locally muted, {@code false} otherwise.
     */
    public boolean isSelfMuted() {
        return data.isSelfMute();
    }

    /**
     * Gets whether this user is streaming using "Go Live".
     *
     * @return {@code true} if this user is streaming using "Go Live", {@code false} otherwise.
     */
    public boolean isSelfStreaming() {
        return Optional.ofNullable(data.isSelfStream()).orElse(false);
    }

    /**
     * Gets whether this user is muted by the current user.
     *
     * @return {@code true} if this user is muted by the current user, {@code false} otherwise.
     */
    public boolean isSuppressed() {
        return data.isSuppress();
    }

    @Override
    public String toString() {
        return "VoiceState{" +
                "data=" + data +
                '}';
    }
}
