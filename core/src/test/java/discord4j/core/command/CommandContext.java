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

package discord4j.core.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Optional;

public interface CommandContext {

    MessageCreateEvent event();

    String command();

    String parameters();

    Mono<MessageChannel> getReplyChannel();

    Mono<PrivateChannel> getPrivateChannel();

    default GatewayDiscordClient getClient() {
        return event().getClient();
    }

    default Message getMessage() {
        return event().getMessage();
    }

    default Optional<User> getAuthor() {
        return event().getMessage().getAuthor();
    }

    default Mono<Boolean> hasPermission(PermissionSet requiredPermissions) {
        return Mono.justOrEmpty(getAuthor().map(User::getId))
                .flatMap(authorId -> getMessage().getChannel().ofType(GuildChannel.class)
                        .flatMap(channel -> channel.getEffectivePermissions(authorId))
                        .map(set -> set.containsAll(requiredPermissions)));
    }

    CommandContext withDirectMessage();

    CommandContext withReplyChannel(Mono<MessageChannel> channelSource);

    CommandContext withScheduler(Scheduler scheduler);

    Mono<Void> sendMessage(MessageCreateSpec spec);

    Mono<Void> sendEmbed(EmbedCreateSpec spec);
}
