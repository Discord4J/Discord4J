package sx.blah.discord.handle.obj;

/**
 * Represents a voice channel.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * Makes the bot user join this voice channel.
	 */
	void join();

	/**
	 * Makes the bot user leave this voice channel.
	 */
	void leave();
}
