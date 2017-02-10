package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

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
	 * This subscribes an {@link IAudioReceiver} to receive audio from all sources regardless of user.
	 *
	 * @param receiver The receiver to subscribe.
	 */
	void subscribeReceiver(IAudioReceiver receiver);

	/**
	 * This subscribes an {@link IAudioReceiver} to receive audio from a specific user specifically.
	 *
	 * @param receiver The receiver to subscribe.
	 * @param user The user to receive audio from.
	 */
	void subscribeReceiver(IAudioReceiver receiver, IUser user);

	/**
	 * This unsubscribes all copies of this {@link IAudioReceiver} instance from receiving audio.
	 *
	 * @param receiver The receiver to unsubscribe.
	 */
	void unsubscribeReceiver(IAudioReceiver receiver);

	/**
	 * Gets the guild this AudioManager instance belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();
}
