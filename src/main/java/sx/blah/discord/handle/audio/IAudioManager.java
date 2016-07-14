package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This class centralizes all audio handling.
 */
public interface IAudioManager {

	/**
	 * Sets the audio provider.
	 *
	 * @param provider The audio provider.
	 */
	void setAudioProvider(IAudioProvider provider);

	/**
	 * Gets the current audio provider.
	 *
	 * @return The audio provider.
	 */
	IAudioProvider getAudioProvider();

	/**
	 * Sets the audio processor.
	 *
	 * @param processor The audio processor.
	 */
	void setAudioProcessor(IAudioProcessor processor);

	/**
	 * Gets the current audio processor.
	 *
	 * @return The audio processor.
	 */
	IAudioProcessor getAudioProcessor();

	/**
	 * Gets the next 20 ms of audio for discord.
	 *
	 * @return The raw, opus-encoded bytes.
	 */
	byte[] getAudio();

	/**
	 * Gets the guild this AudioManager instance belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();
}
