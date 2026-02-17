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

package discord4j.common.close;

import org.jspecify.annotations.Nullable;

/**
 * Encapsulates a strategy to deal with the closing and release of a resource.
 */
public class DisconnectBehavior {

    private final Action action;
    private final Throwable cause;

    /**
     * Create a {@link DisconnectBehavior} that instructs a client to retry in a graceful manner, with an optional cause.
     *
     * @param cause optionally, a {@link Throwable} that triggered this close intent
     * @return a {@link DisconnectBehavior} that will gracefully attempt to retry
     */
    public static DisconnectBehavior retry(@Nullable Throwable cause) {
        return new DisconnectBehavior(Action.RETRY, cause);
    }

    /**
     * Create a {@link DisconnectBehavior} that instructs a client to stop in a graceful manner, with an optional cause.
     *
     * @param cause optionally, a {@link Throwable} that triggered this close intent
     * @return a {@link DisconnectBehavior} that will gracefully stop and release resources
     */
    public static DisconnectBehavior stop(@Nullable Throwable cause) {
        return new DisconnectBehavior(Action.STOP, cause);
    }

    /**
     * Create a {@link DisconnectBehavior} that instructs a client to retry abruptly, with an optional cause.
     *
     * @param cause optionally, a {@link Throwable} that triggered this close intent
     * @return a {@link DisconnectBehavior} that will abruptly close before attempting to retry
     */
    public static DisconnectBehavior retryAbruptly(@Nullable Throwable cause) {
        return new DisconnectBehavior(Action.RETRY_ABRUPTLY, cause);
    }

    /**
     * Create a {@link DisconnectBehavior} that instructs a client to stop abruptly, with an optional cause.
     *
     * @param cause optionally, a {@link Throwable} that triggered this close intent
     * @return a {@link DisconnectBehavior} that will abruptly stop and release resources
     */
    public static DisconnectBehavior stopAbruptly(@Nullable Throwable cause) {
        return new DisconnectBehavior(Action.STOP_ABRUPTLY, cause);
    }

    private DisconnectBehavior(Action action, @Nullable Throwable cause) {
        this.action = action;
        this.cause = cause;
    }

    /**
     * Returns the action to perform while closing a resource.
     *
     * @return an {@link Action} to perform upon closing
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the cause that triggered this close intent.
     *
     * @return a {@link Throwable} representing the cause that triggers an action
     */
    @Nullable
    public Throwable getCause() {
        return cause;
    }

    /**
     * The action to trigger to close a resource.
     */
    public enum Action {
        RETRY, STOP, RETRY_ABRUPTLY, STOP_ABRUPTLY;
    }

    @Override
    public String toString() {
        return String.format("%s%s", action, cause != null ? " due to " + cause : "");
    }
}
