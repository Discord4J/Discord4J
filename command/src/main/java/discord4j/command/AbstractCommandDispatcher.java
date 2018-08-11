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

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;
import java.util.Set;

/**
 * An utility abstract implementation of {@link CommandDispatcher}.
 * <p>
 * The implementation of {@link #dispatch(MessageCreateEvent, Set, CommandErrorHandler)} is designed to cover the vast
 * majority of use cases. It will automatically filter out empty contents and bots and will determine command names by
 * stripping any prefix (provided by {@link #getPrefixes(MessageCreateEvent)}) and determining the command name based
 * off the first sequence of characters before a space or until end-of-string is reached. The rest of the remaining
 * content index is considered to be part of the argument namespace.
 */
public abstract class AbstractCommandDispatcher implements CommandDispatcher {

    @Override
    public Publisher<? extends Command<?>> dispatch(final MessageCreateEvent event,
                                                    final Set<CommandProvider<?>> providers,
                                                    final CommandErrorHandler errorHandler) {
        final Optional<String> content = event.getMessage().getContent();

        return event.getMessage().getAuthor()
                .filter(user -> !user.isBot())
                .filter(ignored -> content.isPresent())
                .flatMapMany(ignored -> getPrefixes(event))
                .filter(content.get()::startsWith)
                .map(prefix -> content.get().substring(prefix.length()).trim())
                .map(trimmed -> { // Returns a Tuple2 which represents the commandName and argument start index
                    final int endIndex = (trimmed.indexOf(' ') < 0) ? trimmed.length() : trimmed.indexOf(' ');
                    final String commandName = trimmed.substring(0, endIndex);
                    final String remaining = trimmed.substring(commandName.length()).trim();
                    return Tuples.of(commandName, content.get().length() - remaining.length());
                }).cache()
                .zipWith(Flux.fromIterable(providers))
                .concatMap(context -> {
                    final Tuple2<String, Integer> hints = context.getT1();
                    final CommandProvider<?> provider = context.getT2();
                    return Flux.from(provider.provide(event, hints.getT1(), hints.getT2(), content.get().length()))
                            .onErrorResume(error -> errorHandler.handle(event, error)
                                    .then(Mono.empty()));
                }).flatMap(context -> context.getCommand().execute(event, context.getContext().orElse(null))
                        .onErrorResume(error -> errorHandler.handle(event, error))
                        .thenReturn(context.getCommand()));
    }

    /**
     * Requests to retrieve the prefixes for the supplied event.
     *
     * @param event The event to retrieve prefixes for.
     * @return A {@link Publisher} that continually emits the prefixes for the supplied event.
     */
    protected abstract Publisher<String> getPrefixes(MessageCreateEvent event);
}
