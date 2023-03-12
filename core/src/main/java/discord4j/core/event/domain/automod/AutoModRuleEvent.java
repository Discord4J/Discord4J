package discord4j.core.event.domain.automod;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Generic class related to AutoModRule events
 */
public class AutoModRuleEvent extends Event {

    private final AutoModRule autoModRule;

    protected AutoModRuleEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, AutoModRule autoModRule) {
        super(gateway, shardInfo);
        this.autoModRule = autoModRule;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return this.autoModRule.getGuildId();
    }

    /**
     * Requests to retrieve the {@link Guild} whose the entry was created.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }
}
