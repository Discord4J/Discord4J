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

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;
import java.util.function.Supplier;

class CommandOperations implements CommandContext {

    private final MessageCreateEvent event;
    private final String command;
    private final String parameters;
    private final Supplier<Mono<MessageChannel>> replyChannel;
    private final Scheduler replyScheduler;

    public CommandOperations(MessageCreateEvent event, String command, String parameters) {
        this.event = event;
        this.command = command;
        this.parameters = parameters;
        this.replyChannel = this::getReplyChannel;
        this.replyScheduler = Schedulers.immediate();
    }

    CommandOperations(MessageCreateEvent event, String command, String parameters,
                      Supplier<Mono<MessageChannel>> replyChannel, Scheduler replyScheduler) {
        this.event = event;
        this.command = command;
        this.parameters = parameters;
        this.replyChannel = replyChannel;
        this.replyScheduler = replyScheduler;
    }

    @Override
    public MessageCreateEvent event() {
        return event;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    public String parameters() {
        return parameters;
    }

    @Override
    public Mono<MessageChannel> getReplyChannel() {
        return event.getMessage().getChannel();
    }

    @Override
    public Mono<PrivateChannel> getPrivateChannel() {
        return Mono.justOrEmpty(event.getMessage().getAuthor()).flatMap(User::getPrivateChannel);
    }

    @Override
    public CommandContext withDirectMessage() {
        return new CommandOperations(event, command, parameters, () -> getPrivateChannel().cast(MessageChannel.class),
                replyScheduler);
    }

    @Override
    public CommandContext withReplyChannel(Mono<MessageChannel> channelSource) {
        return new CommandOperations(event, command, parameters, () -> channelSource, replyScheduler);
    }

    @Override
    public CommandContext withScheduler(Scheduler scheduler) {
        return new CommandOperations(event, command, parameters, replyChannel, scheduler);
    }

    @Override
    public Mono<Void> sendMessage(Consumer<? super MessageCreateSpec> spec) {
        return replyChannel.get()
                .publishOn(replyScheduler)
                .flatMap(channel -> channel.createMessage(spec))
                .then();
    }

    @Override
    public Mono<Void> sendEmbed(Consumer<? super EmbedCreateSpec> spec) {
        return replyChannel.get()
                .publishOn(replyScheduler)
                .flatMap(channel -> channel.createEmbed(spec))
                .then();
    }
}
