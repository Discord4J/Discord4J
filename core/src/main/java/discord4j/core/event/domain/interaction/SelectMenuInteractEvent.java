package discord4j.core.event.domain.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.gateway.ShardInfo;

import java.util.List;

@Experimental
public class SelectMenuInteractEvent extends ComponentInteractEvent {

    public SelectMenuInteractEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    public List<String> getValues() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getValues)
                .orElseThrow(IllegalStateException::new); // should always be present for select menus
    }
}
