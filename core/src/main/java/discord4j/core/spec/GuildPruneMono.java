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
 * Mono used to begin a prune operation.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#begin-guild-prune">Begin Guild Prune</a>
 */
public class GuildPruneMono extends AuditableRequest<Integer, Void, GuildPruneMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final Multimap<String, Object> requestMap;

    public GuildPruneMono(@Nullable String reason, Multimap<String, Object> requestMap,
                          GatewayDiscordClient gateway, long guildId) {
        super(() -> {
            throw new UnsupportedOperationException("GuildPruneMono has no internal builder.");
        }, reason);
        this.requestMap = requestMap;
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public GuildPruneMono(GatewayDiscordClient gateway, long guildId) {
        this(null, new Multimap<>(), gateway, guildId);
    }

    @Override
    public GuildPruneMono withReason(String reason) {
        return new GuildPruneMono(reason, requestMap, gateway, guildId);
    }

    @Override
    GuildPruneMono withBuilder(UnaryOperator<Void> f) {
        throw new UnsupportedOperationException("GuildPruneMono has no internal builder.");
    }

    /**
     * Set the number of days to prune.
     *
     * @param days the number of days
     * @return this mono
     */
    public GuildPruneMono withDays(int days) {
        requestMap.set("days", days);
        return new GuildPruneMono(reason, requestMap, gateway, guildId);
    }

    /**
     * Include a role in the prune request. By default, prune will not remove users with roles, therefore this method
     * can be used to include such users.
     *
     * @param roleId the role ID to include for prune
     * @return this mono
     */
    public GuildPruneMono addRole(Snowflake roleId) {
        requestMap.add("include_roles", roleId.asString());
        return new GuildPruneMono(reason, requestMap, gateway, guildId);
    }

    /**
     * Include multiple roles in the prune request. By default, prune will not remove users with roles, therefore
     * this method can be used to include such users.
     *
     * @param roleIds the role IDs to include for prune
     * @return this mono
     */
    public GuildPruneMono addRoles(Collection<Snowflake> roleIds) {
        requestMap.addAll("include_roles", roleIds.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return new GuildPruneMono(reason, requestMap, gateway, guildId);
    }

    /**
     * Set whether the number of pruned members is returned when this actions completes. By default this is enabled,
     * but this is discouraged on large guilds so you can set it to {@code false}.
     *
     * @param enable whether the pruned total is returned, if {@code false}, the prune action will eventually
     * complete with an empty {@link Mono}.
     * @return this mono
     */
    public GuildPruneMono withComputePruneCount(boolean enable) {
        requestMap.set("compute_prune_count", enable);
        return new GuildPruneMono(reason, requestMap, gateway, guildId);
    }

    @Override
    Mono<Integer> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getGuildService()
                .beginGuildPrune(guildId, requestMap, reason)
                .flatMap(data -> Mono.justOrEmpty(data.pruned())));
    }
}
