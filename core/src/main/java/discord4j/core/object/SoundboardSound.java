package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ImmutableSendSoundboardSoundRequest;
import discord4j.discordjson.json.SendSoundboardSoundRequest;
import discord4j.discordjson.json.SoundboardSoundData;
import reactor.core.publisher.Mono;

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

    public String getName() {
        return this.data.name();
    }

    public double getVolume() {
        return this.data.volume();
    }

    public boolean isAvailable() {
        return this.data.available();
    }

    public Optional<ReactionEmoji> getEmoji() {
        if (this.data.emojiId().isPresent() || this.data.emojiName().isPresent()) {
            return Optional.of(ReactionEmoji.of(EmojiData.builder().id(this.data.emojiId()).name(this.data.emojiName()).build()));
        }
        return Optional.empty();
    }

    public Optional<Snowflake> getGuildId() {
        return this.data.guildId().toOptional().map(Snowflake::of);
    }

    public Optional<User> getUser() {
        return this.data.user().toOptional().map(userData -> new User(this.gateway, userData)) ;
    }

    public Mono<Void> sendSound(Snowflake voiceChannelId) {
        ImmutableSendSoundboardSoundRequest.Builder builder = SendSoundboardSoundRequest.builder();
        builder.soundId(this.data.soundId());
        if (this.getGuildId().isPresent()) {
            builder.sourceGuildId(this.getGuildId().get().asLong());
        }
        return this.getClient().getRestClient().getSoundboardService().sendSoundboardSound(voiceChannelId.asLong(), builder.build());
    }

    public SoundboardSoundData getData() {
        return this.data;
    }

}
