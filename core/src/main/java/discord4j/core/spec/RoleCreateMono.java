package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ImmutableRoleCreateRequest;
import discord4j.discordjson.json.RoleCreateRequest;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class RoleCreateMono extends AuditableRequest<Role, ImmutableRoleCreateRequest.Builder, RoleCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;

    public RoleCreateMono(Supplier<ImmutableRoleCreateRequest.Builder> requestBuilder, @Nullable String reason,
                          GatewayDiscordClient gateway, long guildId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public RoleCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(RoleCreateRequest::builder, null, gateway, guildId);
    }

    @Override
    RoleCreateMono withBuilder(UnaryOperator<ImmutableRoleCreateRequest.Builder> f) {
        return new RoleCreateMono(apply(f), reason, gateway, guildId);
    }

    public RoleCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public RoleCreateMono withPermissions(PermissionSet permissions){
         return withBuilder(it -> it.permissions(permissions.getRawValue()));
    }

    public RoleCreateMono withColor(Color color) {
        return withBuilder(it -> it.color(color.getRGB()));
    }

    public RoleCreateMono withHoist(boolean hoist) {
        return withBuilder(it -> it.hoist(hoist));
    }

    public RoleCreateMono withMentionable(boolean mentionable) {
        return withBuilder(it -> it.mentionable(mentionable));
    }

    @Override
    public RoleCreateMono withReason(String reason) {
        return new RoleCreateMono(requestBuilder, reason, gateway, guildId);
    }

    @Override
    Mono<Role> getRequest() {
        return gateway.getRestClient().getGuildService()
                .createGuildRole(guildId, requestBuilder.get().build(), reason)
                .map(data -> new Role(gateway, data, guildId));
    }
}
