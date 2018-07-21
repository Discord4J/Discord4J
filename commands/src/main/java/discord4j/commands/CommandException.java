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

package discord4j.commands;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * An abstract class for propagating non-fatal, user-facing, command-caused errors.
 */
public class CommandException extends RuntimeException {

    public CommandException(@Nullable String response) {
        super(response, null, false, false); //Prevents expensive stacktrace filling
    }

    /**
     * Returns a human-readable error message for the user who caused this error to read.
     *
     * @return The human-readable error message or empty to not respond.
     */
    public Optional<String> response() {
        return Optional.ofNullable(getMessage());
    }
}
