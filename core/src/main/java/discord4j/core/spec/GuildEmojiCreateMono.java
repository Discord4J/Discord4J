package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.GuildEmojiCreateRequest;
import discord4j.discordjson.json.ImmutableGuildEmojiCreateRequest;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class GuildEmojiCreateMono extends AuditableRequest<GuildEmoji, ImmutableGuildEmojiCreateRequest.Builder, GuildEmojiCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;

    public GuildEmojiCreateMono(Supplier<ImmutableGuildEmojiCreateRequest.Builder> requestBuilder,
                                @Nullable String reason, GatewayDiscordClient gateway, long guildId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public GuildEmojiCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(GuildEmojiCreateRequest::builder, null, gateway, guildId);
    }

    public GuildEmojiCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public GuildEmojiCreateMono withImage(Image image) {
        return withBuilder(it -> it.image(image.getDataUri()));
    }

    public GuildEmojiCreateMono addRole(Snowflake roleId) {
        return withBuilder(it -> it.addRole(roleId.asString()));
    }

    @Override
    GuildEmojiCreateMono withBuilder(UnaryOperator<ImmutableGuildEmojiCreateRequest.Builder> f) {
        return new GuildEmojiCreateMono(apply(f), reason, gateway, guildId);
    }

    @Override
    public GuildEmojiCreateMono withReason(String reason) {
        return new GuildEmojiCreateMono(requestBuilder, reason, gateway, guildId);
    }

    @Override
    Mono<GuildEmoji> getRequest() {
        return gateway.getRestClient().getEmojiService()
                .createGuildEmoji(guildId, requestBuilder.get().build(), reason)
                .map(data -> new GuildEmoji(gateway, data, guildId));
    }
}
