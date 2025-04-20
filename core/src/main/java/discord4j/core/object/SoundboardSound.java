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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.SoundboardSoundEditMono;
import discord4j.core.spec.SoundboardSoundEditSpec;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ImmutableSendSoundboardSoundRequest;
import discord4j.discordjson.json.SendSoundboardSoundRequest;
import discord4j.discordjson.json.SoundboardSoundData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a soundboard sound.
 *
 * @see <a href="https://discord.com/developers/docs/resources/soundboard#soundboard-sound-object">https://discord.com/developers/docs/resources/soundboard#soundboard-sound-object</a>
 */
public class SoundboardSound implements Entity {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final SoundboardSoundData data;

    public SoundboardSound(GatewayDiscordClient gateway, SoundboardSoundData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(this.data.soundId());
    }

    /**
     * Gets the name of the sound.
     *
     * @return the name of the sound
     */
    public String getName() {
        return this.data.name();
    }

    /**
     * Gets the volume of the sound.
     *
     * @return the volumen of sound between 0 and 1
     */
    public double getVolume() {
        return this.data.volume();
    }

    /**
     * Gets whether this sound can be used.
     *
     * @return {@code true} if is available to use, {@code false} otherwise
     */
    public boolean isAvailable() {
        return this.data.available();
    }

    /**
     * Gets the emoji associated with this sound, if present.
     *
     * @return the emoji
     */
    public Optional<ReactionEmoji> getEmoji() {
        if (this.data.emojiId().isPresent() || this.data.emojiName().isPresent()) {
            return Optional.of(ReactionEmoji.of(EmojiData.builder().id(this.data.emojiId()).name(this.data.emojiName()).build()));
        }
        return Optional.empty();
    }

    /**
     * Gets the guild id of the sound, if present.
     *
     * @return the guild id
     */
    public Optional<Snowflake> getGuildId() {
        return this.data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the user who creates the sound, if present.
     *
     * @return a user
     */
    public Optional<User> getUser() {
        return this.data.user().toOptional().map(userData -> new User(this.gateway, userData)) ;
    }

    /**
     * Request sends this sound to a voice channel.
     * <br>
     * <b>Note:</b> this requires the BOT being connected to that voice channel.
     *
     * @param voiceChannelId the channel id of the voice channel to send the sound to.
     * @return a {@link Mono} that completes when the sound has been sent.
     */
    public Mono<Void> sendSound(Snowflake voiceChannelId) {
        ImmutableSendSoundboardSoundRequest.Builder builder = SendSoundboardSoundRequest.builder();
        builder.soundId(this.data.soundId());
        if (this.getGuildId().isPresent()) {
            builder.sourceGuildId(this.getGuildId().get().asLong());
        }
        return this.getClient().getRestClient().getSoundboardService().sendSoundboardSound(voiceChannelId.asLong(), builder.build());
    }

    /**
     * Requests to edit this soundboard sound.
     *
     * @return A {@link Mono} which, upon completion, emits the new {@link SoundboardSound soundboard sound} update. If an error
     * occurs, it is emitted through the {@link Mono}.
     */
    public SoundboardSoundEditMono edit() {
        if (!this.getGuildId().isPresent()) {
            throw new IllegalStateException("Cannot edit a soundboard sound without guild id.");
        }
        return SoundboardSoundEditMono.of(this);
    }

    /**
     * Requests to edit this soundboard sound with the provided spec.
     *
     * @param spec the parameters to update
     * @return A {@link Mono} which, upon completion, emits the new {@link SoundboardSound soundboard sound} update. If an error
     * occurs, it is emitted through the {@link Mono}.
     */
    public Mono<SoundboardSound> edit(SoundboardSoundEditSpec spec) {
        if (!this.getGuildId().isPresent()) {
            return Mono.error(new IllegalStateException("Cannot edit a soundboard sound without guild id."));
        }
        Objects.requireNonNull(spec);
        return Mono.defer(() -> gateway.getRestClient().getSoundboardService().modifyGuildSoundboardSound(this.getGuildId().get().asLong(), this.getId().asLong(), spec.asRequest(), spec.reason())
            .map(data -> new SoundboardSound(gateway, data)));
    }

    /**
     * Request to delete this soundboard sound.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the sound has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return this.delete(null);
    }

    /**
     * Request to delete this soundboard sound.
     *
     * @param reason the reason to remove
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the sound has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        if (!this.getGuildId().isPresent()) {
            return Mono.error(new IllegalStateException("Cannot delete soundboard sound without guild id."));
        }
        return this.getClient().getRestClient().getSoundboardService().deleteGuildSoundboardSound(this.getGuildId().get().asLong(), this.getId().asLong(), reason);
    }

    public SoundboardSoundData getData() {
        return this.data;
    }

}
