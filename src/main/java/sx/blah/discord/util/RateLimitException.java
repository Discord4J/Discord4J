package sx.blah.discord.util;

/**
 * Represents a 429 TOO MANY REQUESTS return code from a url connection.
 * This happens if your bot exceeds a Discord api rate limit.
 */
public class RateLimitException extends Exception {

	private final long retryAfter;
	private final String route;
	private final boolean global;

	public RateLimitException(String message, long retryAfter, String method, boolean global) {
		super(message);
		this.retryAfter = retryAfter;
		this.route = method;
		this.global = global;
	}

	/**
	 * This gets the amount of time (in milliseconds) to wait until sending another request.
	 *
	 * @return The amount of milliseconds to wait before retrying the operation.
	 */
	public long getRetryDelay() {
		return retryAfter;
	}

	/**
	 * Gets the bucket (set of requests) this exception covers.
	 *
	 * @return The bucket.
	 * @deprecated See {@link #getMethod()}
	 */
	@Deprecated
	public String getBucket() {
		return route;
	}

	/**
	 * Gets the route this rate limit was in response to.
	 *
	 * @return The route.
	 * @deprecated See {@link #getRoute()}
	 */
	@Deprecated
	public String getMethod() {
		return route;
	}

	/**
	 * Gets the route this rate limit was in response to.
	 *
	 * @return The route.
	 */
	public String getRoute() {
		return route;
	}

	/**
	 * Gets whether this is a global rate limit or limited to a particular route.
	 *
	 * @return True if global or false if otherwise.
	 */
	public boolean isGlobal() {
		return global;
	}
}
