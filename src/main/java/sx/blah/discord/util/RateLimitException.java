package sx.blah.discord.util;

import sx.blah.discord.json.responses.RateLimitResponse;

/**
 * Represents a 429 TOO MANY REQUESTS return code from a url connection.
 * This happens if your bot exceeds a Discord api rate limit.
 */
public class RateLimitException extends HTTP429Exception {

	private long retryAfter;
	private String bucket;

	public RateLimitException(String message, long retryAfter, String bucket) {
		super(message, retryAfter, bucket);
		this.retryAfter = retryAfter;
		this.bucket = bucket;
	}

	public RateLimitException(RateLimitResponse json) {
		this(json.message, json.retry_after, json.bucket);
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
	 */
	public String getBucket() {
		return bucket;
	}
}
