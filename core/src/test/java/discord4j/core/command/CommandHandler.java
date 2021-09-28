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

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class CommandHandler implements Function<CommandContext, Publisher<Void>>, Predicate<CommandContext> {

    final Predicate<? super CommandContext> condition;
    final Function<? super CommandContext, ? extends Publisher<Void>> handler;

    public CommandHandler(
            Predicate<? super CommandContext> condition,
            Function<? super CommandContext, ? extends Publisher<Void>> handler) {
        this.condition = Objects.requireNonNull(condition, "condition");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public Publisher<Void> apply(CommandContext context) {
        return handler.apply(context);
    }

    @Override
    public boolean test(CommandContext request) {
        return condition.test(request);
    }

    static CommandHandler NOOP_HANDLER = new CommandHandler(ctx -> false, ctx -> Mono.empty());
}
