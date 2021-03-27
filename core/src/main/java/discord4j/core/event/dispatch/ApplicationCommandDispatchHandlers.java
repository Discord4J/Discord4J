package discord4j.core.event.dispatch;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.command.ApplicationCommandCreateEvent;
import discord4j.core.event.domain.command.ApplicationCommandDeleteEvent;
import discord4j.core.event.domain.command.ApplicationCommandUpdateEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.gateway.ApplicationCommandCreate;
import discord4j.discordjson.json.gateway.ApplicationCommandDelete;
import discord4j.discordjson.json.gateway.ApplicationCommandUpdate;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

class ApplicationCommandDispatchHandlers {

    static Mono<ApplicationCommandCreateEvent> applicationCommandCreate(DispatchContext<ApplicationCommandCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ShardInfo shardInfo = context.getShardInfo();
        Long guildId = Long.parseLong(context.getDispatch().guildId());
        ApplicationCommandData command = context.getDispatch().command();

        return Mono.just(new ApplicationCommandCreateEvent(gateway, shardInfo,
                new ApplicationCommand(gateway, command), guildId));
    }

    static Mono<ApplicationCommandUpdateEvent> applicationCommandUpdate(DispatchContext<ApplicationCommandUpdate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ShardInfo shardInfo = context.getShardInfo();
        Long guildId = Long.parseLong(context.getDispatch().guildId());
        ApplicationCommandData command = context.getDispatch().command();

        return Mono.just(new ApplicationCommandUpdateEvent(gateway, shardInfo,
                new ApplicationCommand(gateway, command), guildId));
    }

    static Mono<ApplicationCommandDeleteEvent> applicationCommandDelete(DispatchContext<ApplicationCommandDelete, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ShardInfo shardInfo = context.getShardInfo();
        Long guildId = Long.parseLong(context.getDispatch().guildId());
        ApplicationCommandData command = context.getDispatch().command();

        return Mono.just(new ApplicationCommandDeleteEvent(gateway, shardInfo,
                new ApplicationCommand(gateway, command), guildId));
    }

}
