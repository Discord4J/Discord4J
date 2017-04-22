/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util;

/**
 * This represents an exception thrown when there is a miscellaneous error doing a discord operation.
 */
public class DiscordException extends RuntimeException {

	private String message;

	/**
	 * @param message The error message
	 */
	public DiscordException(String message) {
		super(message);
		this.message = message;
	}

	/**
	 * @param message The error message
	 * @param cause The cause
	 */
	public DiscordException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	/**
	 * This gets the error message sent by Discord.
	 *
	 * @return The error message.
	 */
	public String getErrorMessage() {
		return message;
	}
}
