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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.command;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class CommandBootstrapper {

    public static CommandBootstrapper of(final CommandProcessor processor) {
        return new CommandBootstrapper(processor);
    }

    private final Set<CommandCategory<?>> categories;
    private final CommandProcessor processor;

    private CommandBootstrapper(final CommandProcessor processor) {
        categories = new CopyOnWriteArraySet<>();
        this.processor = processor;
    }

    public Flux<? extends Command<?>> attach(final DiscordClient client) {
        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().isPresent())
                .flatMap(event -> processor.process(event, categories))
                .share();
    }

    public Set<CommandCategory<?>> getCategories() {
        return categories;
    }

    public CommandProcessor getProcessor() {
        return processor;
    }
}
