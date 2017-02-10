package sx.blah.discord.handle.audio;

/**
 * The types of specific audio handling Discord4J supports.
 */
public enum AudioEncodingType {
	/**
	 * When the audio encoding type is PCM, the audio will automatically be converted to opus.
	 */
	PCM,
	/**
	 * When the audio encoding type is opus, the audio is sent directly to discord as is.
	 */
	OPUS,
}
