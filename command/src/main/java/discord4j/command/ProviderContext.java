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

import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Context from a {@link CommandProvider} for a command's execution.
 */
public final class ProviderContext<T> {

    /**
     * Constructs a {@code ProviderContext}.
     *
     * @param command The command for possible execution.
     * @param context The context for the command's execution. May be null.
     * @return A constructed {@code ProviderContext}.
     */
    public static <T> ProviderContext<T> of(final Command<T> command, @Nullable final T context) {
        return new ProviderContext<>(command, context);
    }

    /**
     * Constructs a {@code ProviderContext}.
     *
     * @param command The command for possible execution.
     * @return A constructed {@code ProviderContext}.
     */
    public static <T> ProviderContext<T> of(final Command<T> command) {
        return new ProviderContext<>(command, null);
    }

    /** The command for possibler execution. */
    private final Command<T> command;

    /** The context for the command's execution. May be null. */
    @Nullable
    private final T context;

    /**
     * Constructs a {@code ProviderContext}.
     *
     * @param command The command for possible execution.
     * @param context The context for the command's execution. May be null.
     */
    private ProviderContext(final Command<T> command, @Nullable final T context) {
        this.command = Objects.requireNonNull(command);
        this.context = context;
    }

    /**
     * Gets the command for possible execution.
     *
     * @return The command for possible execution.
     */
    public Command<T> getCommand() {
        return command;
    }

    /**
     * Gets the context for the command's execution, if present.
     *
     * @return The context for the command's execution, if present.
     */
    public Optional<T> getContext() {
        return Optional.ofNullable(context);
    }
}
