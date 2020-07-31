package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class CategoryCreateMono extends AuditableRequest<Category, ImmutableChannelCreateRequest.Builder, CategoryCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;

    public CategoryCreateMono(GatewayDiscordClient gateway, long guildId,
                              Supplier<ImmutableChannelCreateRequest.Builder> requestBuilder,
                              @Nullable String reason) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public CategoryCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(gateway, guildId, () -> ChannelCreateRequest.builder().type(Channel.Type.GUILD_CATEGORY.getValue()), null);
    }

    @Override
    CategoryCreateMono withBuilder(UnaryOperator<ImmutableChannelCreateRequest.Builder> f) {
        return new CategoryCreateMono(gateway, guildId, apply(f), reason);
    }

    public CategoryCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public CategoryCreateMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
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

        return withBuilder(it -> it.permissionOverwrites(raw));
    }

    @Override
    public CategoryCreateMono withReason(String reason) {
        return new CategoryCreateMono(gateway, guildId, requestBuilder, reason);
    }

    @Override
    public Mono<Category> getRequest() {
        return gateway.getRestClient().getGuildService()
                .createGuildChannel(guildId, requestBuilder.get().build(), reason)
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(Category.class);
    }
}
