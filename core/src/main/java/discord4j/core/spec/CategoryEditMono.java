package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class CategoryEditMono extends AuditableRequest<Category, ImmutableChannelModifyRequest.Builder, CategoryEditMono> {

    private final GatewayDiscordClient gateway;
    private final long categoryId;

    public CategoryEditMono(Supplier<ImmutableChannelModifyRequest.Builder> requestBuilder, @Nullable String reason,
                            GatewayDiscordClient gateway, long categoryId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.categoryId = categoryId;
    }

    public CategoryEditMono(GatewayDiscordClient gateway, long categoryId) {
        this(ChannelModifyRequest::builder, null, gateway, categoryId);
    }

    @Override
    CategoryEditMono withBuilder(UnaryOperator<ImmutableChannelModifyRequest.Builder> f) {
        return new CategoryEditMono(apply(f), reason, gateway, categoryId);
    }

    public CategoryEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public CategoryEditMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    public CategoryEditMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
    public CategoryEditMono withReason(String reason) {
        return new CategoryEditMono(requestBuilder, reason, gateway, categoryId);
    }

    @Override
    Mono<Category> getRequest() {
        return gateway.getRestClient().getChannelService()
                .modifyChannel(categoryId, requestBuilder.get().build(), reason)
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(Category.class);
    }
}
