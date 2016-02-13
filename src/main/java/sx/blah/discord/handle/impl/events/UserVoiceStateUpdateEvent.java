package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This is dispatched when the voice state of a user is updated.
 */
public class UserVoiceStateUpdateEvent extends Event {

	/**
	 * The user that has updated.
	 */
	private final IUser user;

	/**
	 * The channel where this happened.
	 */
	private final IVoiceChannel channel;

	/**
	 * Whether or not the user was suppressed.
	 */
	private final boolean suppressed;

	/**
	 * Whether or not the user muted themselves. If the user did not mute
	 * themselves, it was the server.
	 */
	private final boolean selfMute;

	/**
	 * Whether or not the user deafened themselves. If the user did not mute
	 * themselves, it was the server.
	 */
	private final boolean selfDeafen;

	public UserVoiceStateUpdateEvent(IUser user, IVoiceChannel channel, boolean selfMute, boolean selfDeafen, boolean suppress) {
		this.user = user;
		this.channel = channel;
		this.selfMute = selfMute;
		this.selfDeafen = selfDeafen;
		this.suppressed = suppress;
	}

	/**
	 * Retrieves the user that has had their voice status updated.
	 * 
	 * @return The user that had been updated.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Retrieves the channel where the update took place.
	 * 
	 * @return The voice channel where the update took place.
	 */
	public IVoiceChannel getChannel() {
		return channel;
	}

	/**
	 * Checks if the user muted themselves. If not, then it was the server.
	 * 
	 * @return Whether or not the user muted themselves.
	 */
	public boolean isSelfMuted() {
		return selfMute;
	}

	/**
	 * Checks if the user deafened themselves. If not, then it was the server.
	 * 
	 * @return Whether or not the user deafened themselves.
	 */
	public boolean isSelfDeafened() {
		return selfDeafen;
	}

	/**
	 * Checks if the user was moved to the AFK room.
	 * 
	 * @return Whether or not the user was moved to the AFK room.
	 */
	public boolean isSuppresed() {
		return suppressed;
	}
}