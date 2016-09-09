package sx.blah.discord.util;

/**
 * Represents a 429 TOO MANY REQUESTS return code from a url connection.
 * This happens if your bot exceeds a Discord api rate limit.
 */
public class RateLimitException extends Exception {

	private final long retryAfter;
	private final String method;
	private final boolean global;

	public RateLimitException(String message, long retryAfter, String method, boolean global) {
		this.retryAfter = retryAfter;
		this.method = method;
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
		return method;
	}

	/**
	 * Gets the method this rate limit was in response to.
	 *
	 * @return The method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Gets whether this is a global rate limit or limited to a particular method.
	 *
	 * @return True if global or false if otherwise.
	 */
	public boolean isGlobal() {
		return global;
	}
}
