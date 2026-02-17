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
import reactor.util.context.ContextView;

import java.util.Optional;

/**
 * Unchecked exception thrown when closing a websocket session, expectedly or not.
 * <p>
 * Used to wrap an underlying websocket {@link CloseStatus} so clients can retrieve the
 * status code and perform actions after it.
 */
public class CloseException extends RuntimeException {

    private final CloseStatus closeStatus;
    private final ContextView context;

    /**
     * Create a {@link CloseException} with the given status and Reactor context.
     *
     * @param closeStatus the {@link CloseStatus} representing this exception
     * @param context a {@link ContextView} instance representing metadata related to this exception
     */
    public CloseException(CloseStatus closeStatus, ContextView context) {
        this(closeStatus, context, null);
    }

    /**
     * Create a {@link CloseException} with the given status, Reactor context and cause.
     *
     * @param closeStatus the {@link CloseStatus} representing this exception
     * @param context a {@link ContextView} instance providing metadata related to this exception
     * @param cause the cause for this exception
     */
    public CloseException(CloseStatus closeStatus, ContextView context, @Nullable Throwable cause) {
        super(cause);
        this.closeStatus = closeStatus;
        this.context = context;
    }

    /**
     * Return the underlying {@link CloseStatus} that triggered this exception.
     *
     * @return a close status
     */
    public CloseStatus getCloseStatus() {
        return closeStatus;
    }

    /**
     * Return the websocket close code.
     *
     * @return a websocket close code
     */
    public int getCode() {
        return closeStatus.getCode();
    }

    /**
     * Return a websocket close reason, if present.
     *
     * @return an {@link Optional} containing a close reason if present, or empty otherwise
     */
    public Optional<String> getReason() {
        return closeStatus.getReason();
    }

    /**
     * Return the Reactor {@link ContextView} providing metadata about this exception.
     *
     * @return a Reactor context instance
     */
    public ContextView getContext() {
        return context;
    }

    @Override
    public String getMessage() {
        return "WebSocket closed: " + closeStatus.toString()
                + (getCause() != null ? " caused by " + getCause().toString() : "");
    }
}
