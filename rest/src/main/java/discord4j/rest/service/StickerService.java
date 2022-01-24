package discord4j.rest.service;

import discord4j.discordjson.json.StickerData;
import discord4j.discordjson.json.StickerPackData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return Routes.NITRO_STICKER_PACKS_GET.newRequest()
            .exchange(getRouter())
            .bodyToMono(StickerPackData[].class)
            .flatMapMany(Flux::fromArray);
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

}
