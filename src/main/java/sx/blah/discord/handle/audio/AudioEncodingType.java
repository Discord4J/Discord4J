package sx.blah.discord.handle.audio;

/**
 * The types of specific audio handling Discord4J supports.
 */
public enum AudioEncodingType {
	/**
	 * When the audio encoding type is PCM, the audio will automatically be converted to opus when sending audio and it
	 * will be processed as PCM data when received.
	 */
	PCM,
	/**
	 * When the audio encoding type is opus, the audio is sent directly to discord as is when sending audio and it will
	 * be processed as is from discord when received.
	 */
	OPUS
}
