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

import java.util.Optional;

/**
 * Container for WebSocket "close" status codes and reasons.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1"> RFC 6455, Section 7.4.1 "Defined Status Codes"</a>
 */
public class CloseStatus {

    public static final CloseStatus NORMAL_CLOSE = new CloseStatus(1000, "Logging off");
    public static final CloseStatus ABNORMAL_CLOSE = new CloseStatus(1006, null);

    private final int code;
    @Nullable
    private final String reason;

    /**
     * Create a new {@link CloseStatus} instance.
     *
     * @param code the status code
     * @param reason the reason
     */
    public CloseStatus(int code, @Nullable String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Return the websocket close code.
     *
     * @return a websocket close code
     */
    public int getCode() {
        return code;
    }

    /**
     * Return a websocket close reason, if present.
     *
     * @return an {@link Optional} containing a close reason if present, or empty otherwise
     */
    public Optional<String> getReason() {
        return Optional.ofNullable(reason).filter(r -> !r.isEmpty());
    }

    @Override
    public String toString() {
        return code + (reason == null || reason.isEmpty() ? "" : " " + reason);
    }
}
