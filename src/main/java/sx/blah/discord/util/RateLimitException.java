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
 * Thrown when performing an operation with Discord would result in being ratelimited.
 *
 * <p>This should always be preemptively thrown by Discord4J's request system. However, if the user somehow hits an
 * actual HTTP 429 from Discord, it will also be reported through this exception.
 */
public class RateLimitException extends RuntimeException {

	private final long retryAfter;
	private final String method;
	private final boolean global;

	public RateLimitException(String message, long retryAfter, String method, boolean global) {
		super(message);
		this.retryAfter = retryAfter;
		this.method = method;
		this.global = global;
	}

	/**
	 * Gets the amount of time (in milliseconds) to wait until sending another request.
	 *
	 * @return The amount of time (in milliseconds) to wait until sending another request.
	 */
	public long getRetryDelay() {
		return retryAfter;
	}

	/**
	 * Gets the HTTP method the rate limit was in response to.
	 *
	 * @return The HTTP method the rate limit was in response to.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Gets whether this is a global rate limit or limited to a particular method.
	 *
	 * @return Whether this is a global rate limit.
	 */
	public boolean isGlobal() {
		return global;
	}
}
