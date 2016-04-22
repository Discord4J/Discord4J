package sx.blah.discord.api;

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.util.DiscordException;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {

	private String[] loginInfo = new String[0];
	private long timeoutTime = -1L;
	private int maxMissedPingCount = -1;
	private boolean isBot = false;
	private String botToken;
	private boolean isDaemon = false;

	/**
	 * Sets the login info for the client.
	 *
	 * @param email The user's email.
	 * @param password The user's password.
	 * @return The instance of the builder.
	 *
	 * @deprecated The Discord Developers discourage using a user account! This library still supports its usage, but
	 * it is discouraged. Since it is not supported, there may be bugs associated with it which will NOT be fixed as
	 * again, this is discouraged. Use a bot account and {@link #withToken(String)} instead.
	 */
	@Deprecated
	public ClientBuilder withLogin(String email, String password) {
		loginInfo = new String[]{email, password};
		return this;
	}

	/**
	 * Sets the client to be a bot, replacement for {@link #withLogin(String, String)} for bot accounts.
	 *
	 * @param token The bot's token.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withToken(String token) {
		this.botToken = token;
		isBot = true;
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
	 * Creates the discord instance with the desired features
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient build() throws DiscordException {
		if ((loginInfo.length < 2 && !isBot) && botToken == null)
			throw new DiscordException("No login info present!");

		if (isBot) {
			return new DiscordClientImpl(botToken, timeoutTime, maxMissedPingCount, isDaemon);
		} else {
			return new DiscordClientImpl(loginInfo[0], loginInfo[1], timeoutTime, maxMissedPingCount, isDaemon);
		}
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
