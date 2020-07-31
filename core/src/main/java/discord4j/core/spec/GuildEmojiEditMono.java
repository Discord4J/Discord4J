package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import discord4j.discordjson.json.ImmutableGuildEmojiModifyRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class GuildEmojiEditMono extends AuditableRequest<GuildEmoji, ImmutableGuildEmojiModifyRequest.Builder, GuildEmojiEditMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final long emojiId;

    public GuildEmojiEditMono(Supplier<ImmutableGuildEmojiModifyRequest.Builder> requestBuilder,
                              @Nullable String reason, GatewayDiscordClient gateway, long guildId, long emojiId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
        this.emojiId = emojiId;
    }

    public GuildEmojiEditMono(GatewayDiscordClient gateway, long guildId, long emojiId) {
        this(GuildEmojiModifyRequest::builder, null, gateway, guildId, emojiId);
    }

    @Override
    GuildEmojiEditMono withBuilder(UnaryOperator<ImmutableGuildEmojiModifyRequest.Builder> f) {
        return new GuildEmojiEditMono(apply(f), reason, gateway, guildId, emojiId);
    }

    public GuildEmojiEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public GuildEmojiEditMono withRoles(Set<Snowflake> roles) {
        List<String> list = roles.stream().map(Snowflake::asString).collect(Collectors.toList());
        return withBuilder(it -> it.roles(list));
    }

    @Override
    public GuildEmojiEditMono withReason(String reason) {
        return new GuildEmojiEditMono(requestBuilder, reason, gateway, guildId, emojiId);
    }

    @Override
    Mono<GuildEmoji> getRequest() {
        return gateway.getRestClient().getEmojiService()
                .modifyGuildEmoji(guildId, emojiId, requestBuilder.get().build(), reason)
                .map(data -> new GuildEmoji(gateway, data, guildId));
    }
}
