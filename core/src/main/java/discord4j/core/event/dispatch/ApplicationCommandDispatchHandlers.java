/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

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
