package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
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
	 * Whether or not the user muted themselves.
	 */
	private final boolean selfMute;

	/**
	 * Whether or not the server muted the user.
	 */
	private final boolean mute;

	/**
	 * Whether or not the user deafened themselves.
	 */
	private final boolean selfDeafen;

	/**
	 * Whether or not the server deafened the user.
	 */
	private final boolean deafen;

	public UserVoiceStateUpdateEvent(IUser user, IVoiceChannel channel, boolean selfMute, boolean selfDeafen, boolean mute, boolean deafen, boolean suppress) {
		this.user = user;
		this.channel = channel;
		this.selfMute = selfMute;
		this.selfDeafen = selfDeafen;
		this.mute = mute;
		this.deafen = deafen;
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
	 * Checks if the user muted themselves.
	 *
	 * @return Whether or not the user muted themselves.
	 */
	public boolean isSelfMute() {
		return selfMute;
	}

	/**
	 * Checks if the user deafened themselves.
	 *
	 * @return Whether or not the user deafened themselves.
	 */
	public boolean isSelfDeafen() {
		return selfDeafen;
	}

	/**
	 * Checks if the user was moved to the AFK room.
	 *
	 * @return Whether or not the user was moved to the AFK room.
	 */
	public boolean isSuppressed() {
		return suppressed;
	}

	/**
	 * Gets whether the user is muted.
	 *
	 * @return Whether or not the user is muted.
	 */
	public boolean isMute() {
		return mute;
	}

	/**
	 * Gets whether the user is deafened.
	 *
	 * @return Whether or not the user is deafened.
	 */
	public boolean isDeafen() {
		return deafen;
	}
}
