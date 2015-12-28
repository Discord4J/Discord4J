package sx.blah.discord.api;

/**
 * This represents feature modules that This api supports.
 * This allows for bots to be modular by enabling and disabling certain features.
 */
public enum Features {
	
	MESSAGING(FeatureStatus.DEPRECATED), DISCORD_STATUS(FeatureStatus.UNSUPPORTED), 
	ACCOUNT_MANAGEMENT(FeatureStatus.DEPRECATED), CHANNEL_MANAGEMENT(FeatureStatus.DEPRECATED), 
	SERVER_MANAGEMENT(FeatureStatus.UNSUPPORTED), ROLE_MANAGEMENT(FeatureStatus.UNSUPPORTED), 
	VOICE(FeatureStatus.UNSUPPORTED), INVITES(FeatureStatus.DEPRECATED);
	
	public FeatureStatus status;
	
	Features(FeatureStatus status) {
		this.status = status;
	}
	
	/**
	 * The current state of the feature implementation
	 * SUPPORTED = Feature complete
	 * UNSUPPORTED = No implementation available
	 * DEPRECATED = Things may be broken and will get rewritten or removed entirely
	 * EXPERIMENTAL = Still a WIP feature, things are subject to change without notice
	 * READ_ONLY = Feature can only read the status of something without the ability to modify
	 * WRITE_ONLY = Feature can only change the status of something without the ability to get the status
	 */
	public static enum FeatureStatus {
		SUPPORTED, UNSUPPORTED, DEPRECATED, EXPERIMENTAL, READ_ONLY, WRITE_ONLY
	}
}
