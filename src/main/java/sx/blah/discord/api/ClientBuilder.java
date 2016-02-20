package sx.blah.discord.api;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordClientImpl;

import java.util.EnumSet;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {

	private String[] loginInfo = new String[0];
	private long timeoutTime = -1L;
	private int maxMissedPingCount = -1;
	private boolean register = false;

	/**
	 * Sets the login info for the client. This is a REQUIRED step if not using 'withRegister' instead.
	 *
	 * @param email The user's email
	 * @param password The user's password
	 * @return The instance of the builder
	 */
	public ClientBuilder withLogin(String email, String password) {
		return withLoginInfo(email, password, false);
	}
	
	/**
	 * Sets the register info for the client. This is a REQUIRED step if not using 'withLogin' instead.
	 * 
	 * @param username The username you want to temporary register with.
	 * @param invite The absolute (https://discord.gg/<invite-id>) or relative (<invite-id>) channel invite.
	 * @return The instance of the builder
	 */
	public ClientBuilder withRegister(String username, String invite) {
		return withLoginInfo(username, invite, true);
	}
		
	/**
	 * Internal method to set more easily set the login information to either login or register with Discord. 
	 * 
	 * @param name This represents either the user's email (for login) or the username (for register)
	 * @param cred This represents either the user's password (for login) or the invite (either long or short form for register).
	 * @return The instance of the builder
	 */
	private ClientBuilder withLoginInfo(String name, String cred, boolean register) {
		loginInfo = new String[] {name, cred};
		this.register = register;
		return this;
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
	 * Creates the discord instance with the desired features
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient build() throws DiscordException {
		if (loginInfo.length < 2)
			throw new DiscordException("No login info present!");

		//Warnings for the current version of this api.
		for (Features feature : EnumSet.allOf(Features.class)) {
			switch (feature.status) {
				case UNSUPPORTED:
					Discord4J.LOGGER.warn("Feature '{}' is unsupported by Discord4J!", feature.name());
					break;
				case DEPRECATED:
					Discord4J.LOGGER.warn("Feature '{}' has been deprecated by Discord4J! It may not work as intended and may be changed in the future.", feature.name());
					break;
				case EXPERIMENTAL:
					Discord4J.LOGGER.warn("Feature '{}' is experimental! It is still WIP and is incomplete.", feature.name());
					break;
				case READ_ONLY:
				case WRITE_ONLY:
					Discord4J.LOGGER.warn("Feature '{}' is {}!", feature.name(), feature.status.name());
					break;
				case SUPPORTED:
			}
		}
		return new DiscordClientImpl(loginInfo[0], loginInfo[1], register, timeoutTime, maxMissedPingCount);
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
