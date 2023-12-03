package discord4j.core.event.domain.monetization;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when an entitlement is created.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/monetization/entitlements#new-entitlement">New Entitlement</a>
 */
public class EntitlementCreateEvent extends Event {

    private final long entitlementId;
    private final long skuId;
    private final Long subscriptionId;
    private final Long userId;
    private final Long guildId;
    private final int type;
    private final boolean deleted;
    private final String startsAt;
    private final String endsAt;

    public EntitlementCreateEvent(
        GatewayDiscordClient gateway,
        ShardInfo shardInfo,
        long entitlementId,
        long skuId,
        Long subscriptionId,
        Long userId,
        Long guildId,
        int type,
        boolean deleted,
        String startsAt,
        String endsAt
    ) {
        super(gateway, shardInfo);

        this.entitlementId = entitlementId;
        this.skuId = skuId;
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.guildId = guildId;
        this.type = type;
        this.deleted = deleted;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }



}
