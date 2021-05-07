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
package discord4j.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TokenUtil {

    /**
     * Extracts the user's ID from the token used to authenticate requests.
     *
     * @param token The token used to authenticate requests.
     * @return The user's ID.
     */
    public static long getSelfId(Token token) {
        try {
            return Long.parseLong(new String(Base64.getDecoder()
                    .decode(token.asString().split("\\.")[0]), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token, make sure you're using the token from the " +
                    "developer portal Bot section and not the application client secret or public key.", e);
        }
    }

}
