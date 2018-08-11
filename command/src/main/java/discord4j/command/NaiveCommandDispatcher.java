/*
 *  This file is part of Discord4J.
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Set;
import java.util.function.Function;

/**
 * This is a fairly capable {@link discord4j.command.CommandDispatcher} implementation which requires minimal setup
 * thanks to its simplistic logic. This simply performs a lookup on the prefix and then takes the following word to be
 * the command name. Additionally, this dispatcher ignores bots (this is best practice for bot commands!).
 */
public class NaiveCommandDispatcher implements CommandDispatcher {

    private final Function<MessageCreateEvent, String> prefixGenerator;

    public NaiveCommandDispatcher(Function<MessageCreateEvent, String> prefixGenerator) {
        this.prefixGenerator = prefixGenerator;
    }

    public NaiveCommandDispatcher(String prefix) {
        this(e -> prefix);
    }

    @Override
    public Mono<? extends Command> dispatch(MessageCreateEvent event, Set<CommandProvider> providers,
                                            CommandErrorHandler errorHandler) {
        return Mono.defer(() -> Mono.just(prefixGenerator.apply(event)))
                .filter(prefix -> event.getMessage().getContent().isPresent()
                        && event.getMessage().getContent().get().startsWith(prefix))
                .map(prefix -> event.getMessage().getContent().get().substring(prefix.length()).trim())
                .filterWhen(trimmed -> event.getMessage().getAuthor().map(u -> !u.isBot()))
                .map(trimmed -> {
                    int endIndex = trimmed.indexOf(' ') < 0 ? trimmed.length() : trimmed.indexOf(' ');
                    String cmdName = trimmed.substring(0, endIndex);
                    String remaining = trimmed.substring(cmdName.length()).trim();
                    return Tuples.of(cmdName, event.getMessage().getContent().get().length() - remaining.length());
                })
                .cache()
                .flux()
                .zipWith(Flux.fromIterable(providers))
                .concatMap(context -> {
                    Tuple2<String, Integer> hints = context.getT1();
                    CommandProvider provider = context.getT2();
                    return provider.provide(event, hints.getT1(), hints.getT2(),
                            event.getMessage().getContent().get().length());
                })
                .next()
                .flatMap(cmd -> cmd.execute(event)
                        .onErrorResume(CommandException.class, t -> errorHandler.handle(event, t))
                        .thenReturn(cmd));
    }
}
