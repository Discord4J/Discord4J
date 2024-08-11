package discord4j.rest.service;

import discord4j.discordjson.json.GuildStickerCreateRequest;
import discord4j.discordjson.json.GuildStickerModifyRequest;
import discord4j.discordjson.json.StickerData;
import discord4j.discordjson.json.StickerPackData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class StickerService extends RestService {

    public StickerService(Router router) {
        super(router);
    }

    public Mono<StickerData> getSticker(long stickerId) {
        return Routes.STICKER_GET.newRequest(stickerId)
            .exchange(getRouter())
            .bodyToMono(StickerData.class);
    }

    public Flux<StickerPackData> getStickerPacks() {
        return Routes.STICKER_PACKS_GET.newRequest()
            .exchange(getRouter())
            .bodyToMono(StickerPackData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<StickerPackData> getStickerPack(long stickerPackId) {
        return Routes.STICKER_PACK_GET.newRequest(stickerPackId)
            .exchange(getRouter())
            .bodyToMono(StickerPackData.class);
    }

    public Flux<StickerData> getGuildStickers(long guildId) {
        return Routes.GUILD_STICKERS_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(StickerData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<StickerData> getGuildSticker(long guildId, long stickerId) {
        return Routes.GUILD_STICKER_GET.newRequest(guildId, stickerId)
            .exchange(getRouter())
            .bodyToMono(StickerData.class);
    }

    public Mono<StickerData> createGuildSticker(long guildId, GuildStickerCreateRequest request, @Nullable String reason) {
        return Routes.GUILD_STICKER_CREATE.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(StickerData.class);
    }

    public Mono<StickerData> modifyGuildSticker(long guildId, long stickerId, GuildStickerModifyRequest request, @Nullable String reason) {
        return Routes.GUILD_STICKER_MODIFY.newRequest(guildId, stickerId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(StickerData.class);
    }

    public Mono<Void> deleteGuildSticker(long guildId, long stickerId, @Nullable String reason) {
        return Routes.GUILD_STICKER_DELETE.newRequest(guildId, stickerId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

}
