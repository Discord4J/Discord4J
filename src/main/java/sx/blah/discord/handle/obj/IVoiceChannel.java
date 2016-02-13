package sx.blah.discord.handle.obj;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.util.HTTP429Exception;

import java.util.Optional;

/**
 * Represents a voice channel.
 * TODO
 */
public interface IVoiceChannel extends IChannel {

	/**
	 * Edits the channel.
	 *
	 * @param name The new name of the channel.
	 * @param position The new position of the channel.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 */
	void edit(Optional<String> name, Optional<Integer> position) throws DiscordException, MissingPermissionsException, HTTP429Exception;

	void joinChannel();

	void leaveChannel();
}
