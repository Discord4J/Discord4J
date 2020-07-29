package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CategoryCreateMono extends Mono<Category> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final LazyBuilder<ImmutableChannelCreateRequest.Builder> requestBuilder;
    @Nullable
    private final String reason;

    public CategoryCreateMono(GatewayDiscordClient gateway, long guildId,
                              LazyBuilder<ImmutableChannelCreateRequest.Builder> requestBuilder,
                              @Nullable String reason) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.requestBuilder = requestBuilder;
        this.reason = reason;
    }

    public CategoryCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(gateway, guildId, () -> ChannelCreateRequest.builder().type(Channel.Type.GUILD_CATEGORY.getValue()), null);
    }

    public CategoryCreateMono withName(String name) {
        return new CategoryCreateMono(gateway, guildId, requestBuilder.andThen(it -> it.name(name)), reason);
    }

    public CategoryCreateMono withPosition(int position) {
        return new CategoryCreateMono(gateway, guildId, requestBuilder.andThen(it -> it.position(position)), reason);
    }

    public CategoryCreateMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        List<OverwriteData> raw = permissionOverwrites.stream()
                .map(o -> OverwriteData.builder()
                        .id(o.getTargetId().asString())
                        .type(o.getType().getValue())
                        .allow(o.getAllowed().getRawValue())
                        .deny(o.getDenied().getRawValue())
                        .build())
                .collect(Collectors.toList());

        return new CategoryCreateMono(gateway, guildId,
                requestBuilder.andThen(it -> it.permissionOverwrites(Possible.of(raw))), reason);
    }

    public CategoryCreateMono withReason(String reason) {
        return new CategoryCreateMono(gateway, guildId, requestBuilder, reason);
    }

    @Override
    public void subscribe(CoreSubscriber<? super Category> actual) {
        gateway.getRestClient().getGuildService()
                .createGuildChannel(guildId, requestBuilder.get().build(), reason)
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(Category.class)
                .subscribe(actual);
    }
}
