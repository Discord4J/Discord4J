package sx.blah.discord.api;

import sx.blah.discord.Discord4J;

import java.util.EnumSet;

/**
 * Use this as a factory to create {@link DiscordClient} instances
 */
public class ClientBuilder {
	
	private String[] loginInfo = new String[0];
	private EnumSet<Features> features = EnumSet.allOf(Features.class);
	
	/**
	 * Sets the login info for the client. This is a REQUIRED step
	 * @param email The user's email
	 * @param password The user's password
	 * @return The instance of the builder
	 */
	public ClientBuilder withLogin(String email, String password) {
		loginInfo = new String[] {email, password};
		return this;
	}
	
	/**
	 * Adds features available to the client instance. The builder defaults with
	 * @param features The features the client is allowed to use
	 * @return The instance of the builder
	 */
	public ClientBuilder withFeatures(EnumSet<Features> features) {
		for (Features feature : features) {
			if (!this.features.contains(feature))
				this.features.add(feature);
		}
		return this;
	}
	
	/**
	 * Removes features available to the client instance
	 * @param features The features the client can't use
	 * @return The instance of the builder
	 */
	public ClientBuilder withoutFeatures(EnumSet<Features> features) {
		for (Features feature : features) {
			if (this.features.contains(feature))
				this.features.remove(feature);
		}
		return this;
	}
	
	/**
	 * Creates the discord instance with the desired features
	 * @return The discord instance
	 * @throws DiscordInstantiationException Thrown if the instance isn't built correctly
	 */
	public DiscordClient build() throws DiscordInstantiationException {
		if (loginInfo.length < 2)
			throw new DiscordInstantiationException("No login info present!");
		
		for (Features feature : features) {
			switch (feature.status) {
				case UNSUPPORTED:
					Discord4J.logger.warn("Feature '"+feature.name()+"' is unsupported by Discord4J!");
					break;
				case DEPRECATED:
					Discord4J.logger.warn("Feature '"+feature.name()+"' has been deprecated by Discord4J! It may not work as intended or may be changed in the future.");
					break;
				case EXPERIMENTAL:
					Discord4J.logger.warn("Feature '"+feature.name()+"' is experimental! It is still WIP and is incomplete.");
					break;
				case SUPPORTED:
				case READ_ONLY:
				case WRITE_ONLY:
			}
		}
		return new DiscordClient(loginInfo[0], loginInfo[1], features);
	}
	
	/**
	 * Performs {@link #build()} and logs in automatically
	 * @return The discord instance
	 * @throws DiscordInstantiationException Thrown if the instance isn't built correctly
	 */
	public DiscordClient login() throws DiscordInstantiationException {
		DiscordClient client = build();
		try {
			client.login();
		} catch (Exception e) {
			throw new DiscordInstantiationException("Exception ("+e.getClass().getSimpleName()+") occurred while logging in: "+e.getMessage());
		}
		return client;
	}
}
