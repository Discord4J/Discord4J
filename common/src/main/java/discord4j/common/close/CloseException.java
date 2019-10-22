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

import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

/**
 * Unchecked exception thrown when a websocket session is closed, in an expected way or not.
 * <p>
 * Used to wrap an underlying websocket {@link CloseStatus} so clients can retrieve the
 * status code and perform actions after it.
 */
public class CloseException extends RuntimeException {

    private final CloseStatus closeStatus;
    private final Context context;

    public CloseException(CloseStatus closeStatus, Context context) {
        this(closeStatus, context, null);
    }

    public CloseException(CloseStatus closeStatus, Context context, @Nullable Throwable cause) {
        super(cause);
        this.closeStatus = closeStatus;
        this.context = context;
    }

    public CloseStatus getCloseStatus() {
        return closeStatus;
    }

    public int getCode() {
        return closeStatus.getCode();
    }

    @Nullable
    public String getReason() {
        return closeStatus.getReason();
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getMessage() {
        return "WebSocket closed: " + closeStatus.toString()
                + (getCause() != null ? " caused by " + getCause().toString() : "");
    }
}
