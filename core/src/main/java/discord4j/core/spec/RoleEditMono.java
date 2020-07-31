package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ImmutableRoleModifyRequest;
import discord4j.discordjson.json.RoleModifyRequest;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class RoleEditMono extends AuditableRequest<Role, ImmutableRoleModifyRequest.Builder, RoleEditMono> {

    private final GatewayDiscordClient gateway;
    private final long roleId;
    private final long guildId;

    public RoleEditMono(GatewayDiscordClient gateway, long roleId, long guildId,
                        Supplier<ImmutableRoleModifyRequest.Builder> requestBuilder, @Nullable String reason) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.roleId = roleId;
        this.guildId = guildId;
    }

    public RoleEditMono(GatewayDiscordClient gateway, long roleId, long guildId) {
        this(gateway, roleId, guildId, RoleModifyRequest::builder, null);
    }

    @Override
    RoleEditMono withBuilder(UnaryOperator<ImmutableRoleModifyRequest.Builder> f) {
        return new RoleEditMono(gateway, roleId, guildId, apply(f), reason);
    }

    public RoleEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public RoleEditMono withPermissions(PermissionSet permissions) {
        return withBuilder(it -> it.permissions(permissions.getRawValue()));
    }

    public RoleEditMono withColor(Color color) {
        return withBuilder(it -> it.color(color.getRGB()));
    }

    public RoleEditMono withHoist(boolean hoist) {
        return withBuilder(it -> it.hoist(hoist));
    }

    public RoleEditMono withMentionable(boolean mentionable) {
        return withBuilder(it -> it.mentionable(true));
    }

    @Override
    public RoleEditMono withReason(String reason) {
        return new RoleEditMono(gateway, roleId, guildId, requestBuilder, reason);
    }

    @Override
    Mono<Role> getRequest() {
        return gateway.getRestClient().getGuildService()
                .modifyGuildRole(guildId, roleId, requestBuilder.get().build(), reason)
                .map(data -> new Role(gateway, data, guildId));
    }
}
