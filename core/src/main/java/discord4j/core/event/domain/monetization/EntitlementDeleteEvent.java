package discord4j.core.event.domain.monetization;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.monetization.Entitlement;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Fires when a user's entitlement is deleted. Entitlement deletions are infrequent, and occur when Discord issues a
 * refund for a subscription or when Discord removes an entitlement from a user via internal tooling.
 * Entitlements are <b>not</b> deleted when they expire.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/monetization/entitlements#new-entitlement">New Entitlement</a>
 */
public class EntitlementDeleteEvent extends Event {

    private final Entitlement entitlement;

    public EntitlementDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Entitlement entitlement) {
        super(gateway, shardInfo);

        this.entitlement = entitlement;
    }

    /**
     * Gets the entitlement that was created.
     *
     * @return The entitlement that was created.
     */
    public Entitlement getEntitlement() {
        return entitlement;
    }

    /**
     * Gets the user associated with this entitlement.
     * Only present if the entitlement corresponds to a user subscription.
     *
     * @return A {@link Mono} where, upon successful completion, emits the user associated with this entitlement
     * or empty if the entitlement does not correspond to a user subscription.
     */
    public Mono<User> getUser() {
        return entitlement.getUserId().map(getClient()::getUserById).orElse(Mono.empty());
    }

    /**
     * Gets the guild associated with this entitlement.
     * Only present if the entitlement corresponds to a guild subscription.
     *
     * @return A {@link Mono} where, upon successful completion, emits the guild associated with this entitlement
     * or empty if the entitlement does not correspond to a guild subscription.
     */
    public Mono<Guild> getGuild() {
        return entitlement.getGuildId().map(getClient()::getGuildById).orElse(Mono.empty());
    }

}
