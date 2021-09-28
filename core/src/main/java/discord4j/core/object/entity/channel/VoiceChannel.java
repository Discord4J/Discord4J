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
package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.legacy.LegacyVoiceChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/** A Discord voice channel. */
public final class VoiceChannel extends AudioChannel {

    /**
     * Constructs an {@code VoiceChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data    The raw data as represented by Discord, must be non-null.
     */
    public VoiceChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the user limit of this voice channel.
     *
     * @return The user limit of this voice channel.
     */
    public int getUserLimit() {
        return getData().userLimit().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the camera video quality mode of the voice channel.
     *
     * @return The camera video quality mode of the voice channel.
     */
    public Mode getVideoQualityMode() {
        return getData().videoQualityMode().toOptional().map(Mode::of).orElse(Mode.AUTO);
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyVoiceChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(VoiceChannelEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<VoiceChannel> edit(final Consumer<? super LegacyVoiceChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyVoiceChannelEditSpec mutatedSpec = new LegacyVoiceChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(VoiceChannel.class);
    }

    /**
     * Requests to edit this voice channel. Properties specifying how to edit this voice channel can be set via the
     * {@code withXxx} methods of the returned {@link VoiceChannelEditMono}.
     *
     * @return A {@link VoiceChannelEditMono} where, upon successful completion, emits the edited {@link VoiceChannel}.
     * If an error is received, it is emitted through the {@code VoiceChannelEditMono}.
     */
    public AudioChannelEditMono edit() {
        return VoiceChannelEditMono.of(this);
    }

    /**
     * Requests to edit this voice channel.
     *
     * @param spec an immutable object that specifies how to edit this voice channel
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(VoiceChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(VoiceChannel.class);
    }

    @Override
    public String toString() {
        return "VoiceChannel{} " + super.toString();
    }

    /** Represents the various video quality modes. */
    public enum Mode {

        /** Unknown type. */
        UNKNOWN(-1),

        /** Discord chooses the quality for optimal performance. */
        AUTO(1),

        /** 720p */
        FULL(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code VoiceChannel.Mode}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Mode(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the video quality mode. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The the video quality mode.
         */
        public static VoiceChannel.Mode of(final int value) {
            switch (value) {
                case 1: return AUTO;
                case 2: return FULL;
                default: return UNKNOWN;
            }
        }
    }
}
