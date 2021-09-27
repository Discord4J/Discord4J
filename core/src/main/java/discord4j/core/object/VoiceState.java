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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * A Discord voice state.
 *
 * @see <a href="https://discord.com/developers/docs/resources/voice#voice-resource">Voice Resource</a>
 */
public final class VoiceState implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final VoiceStateData data;

    /**
     * Constructs a {@code VoiceState} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public VoiceState(final GatewayDiscordClient gateway, final VoiceStateData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the voice state.
     *
     * @return The data of the voice state.
     */
    public VoiceStateData getData() {
        return data;
    }

    /**
     * Gets the guild ID this voice state is for.
     *
     * @return The guild ID this voice state is for.
     */
    public Snowflake getGuildId() {
        // Even though Discord's raw Voice State structure does not include the guild_id key when sent in the
        // voice_states for a guild_create, we manually populate the field in GuildDispatchHandlers.guildCreate, so
        // it should always be present, making this safe.
        return Snowflake.of(data.guildId().toOptional().orElseThrow(IllegalStateException::new));
    }

    /**
     * Requests to retrieve the guild this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this voice state is for, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the channel ID this user is connected to, if present.
     *
     * @return The channel ID this user is connected to, if present.
     */
    public Optional<Snowflake> getChannelId() {
        return data.channelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel this user is connected to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} this user is connected
     * to, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<AudioChannel> getChannel() {
        return Mono.justOrEmpty(getChannelId()).flatMap(gateway::getChannelById).cast(AudioChannel.class);
    }

    /**
     * Requests to retrieve the channel this user is connected to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} this user is connected
     * to, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<AudioChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(AudioChannel.class);
    }

    /**
     * Gets the user ID this voice state is for.
     *
     * @return The user ID this voice state is for.
     */
    public Snowflake getUserId() {
        return Snowflake.of(data.userId());
    }

    /**
     * Requests to retrieve the user this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return gateway.getUserById(getUserId());
    }

    /**
     * Requests to retrieve the user this voice state is for, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the user
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getUserId());
    }

    /**
     * Requests to retrieve the member this voice state is for.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return gateway.getMemberById(getGuildId(), getUserId());
    }

    /**
     * Requests to retrieve the member this voice state is for, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the member
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} this voice state is for. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getMemberById(getGuildId(), getUserId());
    }

    /**
     * Gets the session ID for this voice state.
     *
     * @return The session ID for this voice state.
     */
    public String getSessionId() {
        return data.sessionId();
    }

    /**
     * Gets whether this user is deafened by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isDeaf() {
        return data.deaf();
    }

    /**
     * Gets whether this user is muted by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isMuted() {
        return data.mute();
    }

    /**
     * Gets whether this user is locally deafened.
     *
     * @return {@code true} if this user is locally deafened, {@code false} otherwise.
     */
    public boolean isSelfDeaf() {
        return data.selfDeaf();
    }

    /**
     * Gets whether this user is locally muted.
     *
     * @return {@code true} if this user is locally muted, {@code false} otherwise.
     */
    public boolean isSelfMuted() {
        return data.selfMute();
    }

    /**
     * Gets whether this user is streaming using "Go Live".
     *
     * @return {@code true} if this user is streaming using "Go Live", {@code false} otherwise.
     */
    public boolean isSelfStreaming() {
        return data.selfStream().toOptional().orElse(false);
    }

    /**
     * Gets whether this user's camera is enabled.
     *
     * @return {@code true} if this user's camera is enabled, {@code false} otherwise.
     */
    public boolean isSelfVideoEnabled() {
        return data.selfVideo();
    }

    /**
     * Gets whether this user is muted by the current user.
     *
     * @return {@code true} if this user is muted by the current user, {@code false} otherwise.
     */
    public boolean isSuppressed() {
        return data.suppress();
    }

    /**
     * Gets the time at which the user requested to speak, if present.
     *
     * @return The time at which the user requested to speak, if present.
     */
    public Optional<Instant> getRequestedToSpeakAt() {
        return data.requestToSpeakTimestamp()
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    @Override
    public String toString() {
        return "VoiceState{" +
                "data=" + data +
                '}';
    }
}
