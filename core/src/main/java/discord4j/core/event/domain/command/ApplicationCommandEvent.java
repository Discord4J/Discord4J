package discord4j.core.event.domain.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.ShardInfo;

/**
 * Represents an event related to a {@link discord4j.core.object.command.ApplicationCommand}.
 */
public class ApplicationCommandEvent extends Event {

    public ApplicationCommandEvent(GatewayDiscordClient gateway, ShardInfo shardInfo) {
        super(gateway, shardInfo);
    }
}
