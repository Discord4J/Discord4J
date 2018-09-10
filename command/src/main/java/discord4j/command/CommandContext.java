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

import javax.annotation.Nullable;
import java.util.Optional;

public final class CommandContext<T> {

    public static <T> CommandContext<T> of(final Command<T> command, final T context) {
        return new CommandContext<>(command, context, null);
    }

    public static <T> CommandContext<T> of(final Command<T> command) {
        return new CommandContext<>(command, null, null);
    }

    public static <T> CommandContext<T> of(final HelpPage helpPage) {
        return new CommandContext<>(null, null, helpPage);
    }

    @Nullable
    private final Command<T> command;

    @Nullable
    private final T context;

    @Nullable
    private final HelpPage helpPage;

    private CommandContext(@Nullable final Command<T> command,
                           @Nullable final T context,
                           @Nullable final HelpPage helpPage) {
        this.command = command;
        this.context = context;
        this.helpPage = helpPage;
    }

    public Optional<Command<T>> getCommand() {
        return Optional.ofNullable(command);
    }

    public Optional<T> getContext() {
        return Optional.ofNullable(context);
    }

    public Optional<HelpPage> getHelpPage() {
        return Optional.ofNullable(helpPage);
    }
}
