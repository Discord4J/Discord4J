package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.GuildStickerModifyRequest;
import discord4j.discordjson.json.StickerData;
import discord4j.rest.RestClient;
import discord4j.rest.util.Permission;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import java.util.Objects;

/**
 * Represents a guild sticker entity in Discord.
 */
public class RestSticker {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestSticker(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestSticker} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param guildId the ID of the guild this sticker belongs to
     * @param id the ID of this member
     * @return a {@code RestSticker} represented by the given parameters.
     */
    public static RestSticker create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestSticker(restClient, guildId.asLong(), id.asLong());
    }

    static RestSticker create(RestClient restClient, long guildId, long id) {
        return new RestSticker(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this sticker belongs to.
     *
     * @return The ID of the guild this sticker belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this sticker.
     *
     * @return The ID of this sticker
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Return this sticker's parent {@link RestGuild}. This method does not perform any API request.
     *
     * @return the parent {@code RestGuild} of this guild sticker.
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    /**
     * Retrieve this guild sticker's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link StickerData} belonging to this entity.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<StickerData> getData() {
        return restClient.getStickerService().getGuildSticker(guildId, id);
    }

    /**
     * Modify this guild sticker. Requires the {@link Permission#MANAGE_EMOJIS_AND_STICKERS} permission. Returns the updated sticker
     * object on success.
     *
     * @param request the guild sticker modify request
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits the updated {@link StickerData} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<StickerData> modify(GuildStickerModifyRequest request, @Nullable String reason) {
        return restClient.getStickerService().modifyGuildSticker(guildId, id, request, reason);
    }

    /**
     * Delete this guild sticker. Requires the {@link Permission#MANAGE_EMOJIS_AND_STICKERS} permission. Returns empty on success.
     *
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits a complete signal on success. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getStickerService().deleteGuildSticker(guildId, id, reason);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestSticker restSticker = (RestSticker) o;
        return guildId == restSticker.guildId && id == restSticker.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, id);
    }
}
