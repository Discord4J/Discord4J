package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ImmutableRoleModifyRequest;
import discord4j.discordjson.json.RoleModifyRequest;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public final class RoleEditMono extends Mono<Role> {

    private final GatewayDiscordClient gateway;
    private final long roleId;
    private final long guildId;
    private final LazyBuilder<ImmutableRoleModifyRequest.Builder> requestBuilder;
    @Nullable
    private final String reason;

    public RoleEditMono(GatewayDiscordClient gateway, long roleId, long guildId,
                        LazyBuilder<ImmutableRoleModifyRequest.Builder> requestBuilder, @Nullable String reason) {
        this.gateway = gateway;
        this.roleId = roleId;
        this.guildId = guildId;
        this.requestBuilder = requestBuilder;
        this.reason = reason;
    }

    public RoleEditMono(GatewayDiscordClient gateway, long roleId, long guildId) {
        this(gateway, roleId, guildId, RoleModifyRequest::builder, null);
    }

    public RoleEditMono withName(String name) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder.andThen(it -> it.name(name)), reason);
    }

    public RoleEditMono withPermissions(PermissionSet permissions) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder.andThen(it -> it.permissions(permissions.getRawValue())), reason);
    }

    public RoleEditMono withColor(Color color) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder.andThen(it -> it.color(color.getRGB())), reason);
    }

    public RoleEditMono withHoist(boolean hoist) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder.andThen(it -> it.hoist(hoist)), reason);
    }

    public RoleEditMono withMentionable(boolean mentionable) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder.andThen(it -> it.mentionable(mentionable)),reason);
    }

    public RoleEditMono withReason(String reason) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder, reason);
    }

    @Override
    public void subscribe(CoreSubscriber<? super Role> actual) {
        gateway.getRestClient().getGuildService()
                .modifyGuildRole(guildId, roleId, requestBuilder.get().build(), reason)
                .map(data -> new Role(gateway, data, guildId))
                .subscribe(actual);
    }
}
