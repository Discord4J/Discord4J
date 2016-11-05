package sx.blah.discord.api;

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.util.DiscordException;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {

	private long timeoutTime = -1L;
	private int maxMissedPingCount = -1;
	private String botToken;
	private boolean isDaemon = false;
	private int shardCount = 1;

	/**
	 * Provides the login info for the client.
	 *
	 * @param token The bot's token.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withToken(String token) {
		this.botToken = token;
		return this;
	}

	/**
	 * Gets the provided token.
	 *
	 * @return The provided token.
	 */
	public String getToken() {
		return botToken;
	}

	/**
	 * Makes the client have a timeout.
	 *
	 * @param timeoutDelay The timeout delay (in ms).
	 * @return The instance of the builder.
	 */
	public ClientBuilder withTimeout(long timeoutDelay) {
		this.timeoutTime = timeoutDelay;
		return this;
	}

	/**
	 * Makes the client have a ping timeout.
	 *
	 * @param maxMissedPings The maximum amount of pings that discord can not respond to before disconnecting.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withPingTimeout(int maxMissedPings) {
		this.maxMissedPingCount = maxMissedPings;
		return this;
	}

	/**
	 * Sets whether the client should act as a daemon (it is NOT a daemon by default).
	 *
	 * @param isDaemon If true, the client will not stop the JVM from closing until the client is logged out from. If false
	 * the client will stop the JVM from closing until logged out from.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
		return this;
	}

	/**
	 * Sets the sharding information for the client.
	 * @param shardCount The total number of shards that will be created.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withShards(int shardCount) {
		this.shardCount = shardCount;
		return this;
	}

	/**
	 * Creates the discord instance with the desired features
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient build() throws DiscordException {
		if (botToken == null)
			throw new DiscordException("No login info present!");

		return new DiscordClientImpl(botToken, timeoutTime, maxMissedPingCount, isDaemon, shardCount);
	}

	/**
	 * Performs {@link #build()} and logs in automatically
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient login() throws DiscordException {
		IDiscordClient client = build();
		try {
			client.login();
		} catch (Exception e) {
			throw new DiscordException("Exception ("+e.getClass().getSimpleName()+") occurred while logging in: "+e.getMessage());
		}
		return client;
	}
}
