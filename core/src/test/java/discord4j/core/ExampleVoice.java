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

package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.command.CommandListener;
import discord4j.core.support.AddRandomReaction;
import discord4j.core.support.Commands;
import discord4j.core.support.VoiceSupport;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import static discord4j.core.support.Commands.isAuthor;

public class ExampleVoice {

    private static final Logger log = Loggers.getLogger(ExampleVoice.class);

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token"))
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .login()
                .block();

        Mono<Long> ownerId = client.rest().getApplicationInfo()
                .map(ApplicationInfoData::owner)
                .map(Possible::toOptional)
                .flatMap(Mono::justOrEmpty)
                .map(user -> Snowflake.asLong(user.id()))
                .cache();

        CommandListener listener = CommandListener.createWithPrefix("!!")
                .filter(req -> isAuthor(ownerId, req))
                .on("echo", Commands::echo)
                .on("exit", ctx -> ctx.getClient().logout())
                .on("status", Commands::status)
                .on("requestMembers", Commands::requestMembers)
                .on("getMembers", Commands::getMembers)
                .on("addRole", Commands::addRole)
                .on("changeAvatar", Commands::changeAvatar)
                .on("changeLogLevel", Commands::logLevelChange)
                .on("react", new AddRandomReaction())
                .on("userinfo", Commands::userInfo)
                .on("reactionRemove", Commands::reactionRemove)
                .on("leaveGuild", Commands::leaveGuild);

        Mono.when(client.on(listener), VoiceSupport.create(client).eventHandlers()).block();
    }
}
