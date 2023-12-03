package discord4j.core.event.domain.monetization;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;

public class EntitlementUpdateEvent extends Event {

    public EntitlementUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo) {
        super(gateway, shardInfo);
    }

}
