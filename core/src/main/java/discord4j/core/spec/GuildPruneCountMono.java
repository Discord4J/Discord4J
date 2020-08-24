package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Multimap;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collection;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to retrieve the number of members that would be removed in a prune operation.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-prune-count">Get Guild Prune Count</a>
 */
public class GuildPruneCountMono extends AuditableRequest<Integer, Void, GuildPruneCountMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final Multimap<String, Object> requestMap;

    public GuildPruneCountMono(@Nullable String reason, Multimap<String, Object> requestMap,
                               GatewayDiscordClient gateway, long guildId) {
        super(() -> {
            throw new UnsupportedOperationException("GuildPruneCountMono has no internal builder.");
        }, reason);
        this.requestMap = requestMap;
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public GuildPruneCountMono(GatewayDiscordClient gateway, long guildId) {
        this(null, new Multimap<>(), gateway, guildId);
    }

    @Override
    public GuildPruneCountMono withReason(String reason) {
        return new GuildPruneCountMono(reason, requestMap, gateway, guildId);
    }

    @Override
    GuildPruneCountMono withBuilder(UnaryOperator<Void> f) {
        throw new UnsupportedOperationException("GuildPruneCountMono has no internal builder.");
    }

    /**
     * Set the number of days to count prune for.
     *
     * @param days the number of days
     * @return this mono
     */
    public GuildPruneCountMono withDays(int days) {
        requestMap.set("days", days);
        return new GuildPruneCountMono(reason, requestMap, gateway, guildId);
    }

    /**
     * Include a role in the prune count request. By default, prune will not remove users with roles, therefore this
     * method can be used to include such users.
     *
     * @param roleId the role ID to include
     * @return this mono
     */
    public GuildPruneCountMono addRole(Snowflake roleId) {
        requestMap.add("include_roles", roleId.asString());
        return new GuildPruneCountMono(reason, requestMap, gateway, guildId);
    }

    /**
     * Include multiple roles in the prune count request. By default, prune will not remove users with roles,
     * therefore this method can be used to include such users.
     *
     * @param roleIds the role IDs to include
     * @return this mono
     */
    public GuildPruneCountMono addRoles(Collection<Snowflake> roleIds) {
        requestMap.addAll("include_roles", roleIds.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return new GuildPruneCountMono(reason, requestMap, gateway, guildId);
    }

    @Override
    Mono<Integer> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getGuildService()
                .getGuildPruneCount(guildId, requestMap)
                .flatMap(data -> Mono.justOrEmpty(data.pruned())));
    }
}
