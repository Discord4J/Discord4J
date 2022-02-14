package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.GuildStickerEditMono;
import discord4j.core.spec.GuildStickerEditSpec;
import discord4j.discordjson.json.StickerData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;
import java.util.Objects;

public final class GuildSticker extends Sticker {

    /** The ID of the guild this sticker is associated to. */
    private final long guildId;

    public GuildSticker(GatewayDiscordClient gateway, StickerData data) {
        super(gateway, data);
        this.guildId = data.guildId().toOptional().get().asLong();
    }

    public GuildSticker(GatewayDiscordClient gateway, StickerData data, final long guildId) {
        super(gateway, data);
        this.guildId = guildId;
    }

    /**
     * Gets the ID of the guild this user is associated to.
     *
     * @return The ID of the guild this user is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the guild this user is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this user is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Requests to edit this guild sticker. Properties specifying how to edit this sticker can be set via the {@code
     * withXxx} methods of the returned {@link GuildStickerEditMono}.
     *
     * @return A {@link GuildStickerEditMono} where, upon successful completion, emits the edited {@link GuildSticker}. If
     * an error is received, it is emitted through the {@code GuildStickerEditMono}.
     */
    public GuildStickerEditMono edit() {
        return GuildStickerEditMono.of(this);
    }

    /**
     * Requests to edit this guild sticker.
     *
     * @param spec an immutable object that specifies how to edit this sticker
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildSticker}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildSticker> edit(GuildStickerEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getStickerService()
                    .modifyGuildSticker(getGuildId().asLong(), getId().asLong(), spec.asRequest(),
                        spec.reason()))
            .map(data -> new GuildSticker(gateway, data, getGuildId().asLong()));
    }

    /**
     * Requests to delete this sticker.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the sticker has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this sticker while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the sticker has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getStickerService()
            .deleteGuildSticker(getGuildId().asLong(), getId().asLong(), reason);
    }
}
