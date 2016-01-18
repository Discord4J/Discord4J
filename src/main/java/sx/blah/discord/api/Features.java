package sx.blah.discord.api;

/**
 * This represents feature modules that this api supports.
 * This allows for progressive api versions to warn bot developers about changes to the api.
 */
public enum Features {
	
	MESSAGING(FeatureStatus.SUPPORTED), DISCORD_STATUS(FeatureStatus.SUPPORTED),
	ACCOUNT_MANAGEMENT(FeatureStatus.SUPPORTED), CHANNEL_MANAGEMENT(FeatureStatus.SUPPORTED),
	PERMISSIONS(FeatureStatus.SUPPORTED), SERVER_MANAGEMENT(FeatureStatus.SUPPORTED),
	ROLE_MANAGEMENT(FeatureStatus.SUPPORTED), VOICE(FeatureStatus.UNSUPPORTED), INVITES(FeatureStatus.SUPPORTED);
	
	/**
	 * How the feature has been implemented into the api
	 */
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
	public enum FeatureStatus {
		SUPPORTED, UNSUPPORTED, DEPRECATED, EXPERIMENTAL, READ_ONLY, WRITE_ONLY
	}
}
