package discord4j.core.event.domain.automod;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.automod.AutoModRule;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when an automod rule is updated.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway-events#auto-moderation-rule-update">Auto Moderation Rule Update</a>
 */
public class AutoModRuleUpdateEvent extends AutoModRuleEvent {

    public AutoModRuleUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, AutoModRule autoModRule) {
        super(gateway, shardInfo, autoModRule);
    }
}
