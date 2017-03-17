package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when either the client loses connection to discord or is logged out.
 */
public class VoiceDisconnectedEvent extends VoiceChannelEvent {

	private final Reason reason;

	public VoiceDisconnectedEvent(IVoiceChannel channel, Reason reason) {
		super(channel);
		this.reason = reason;
	}

	public VoiceDisconnectedEvent(IGuild guild, Reason reason) {
		super(guild, guild.getConnectedVoiceChannel());
		this.reason = reason;
	}

	/**
	 * Gets the reason this client disconnected.
	 *
	 * @return The reason.
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * This enum represents the possible reasons for discord being disconnected.
	 */
	public enum Reason {
		/**
		 * The user left the voice channel.
		 */
		LEFT_CHANNEL,

		/**
		 * The voice server has been updated and is moving. (Most likely voice region change)
		 */
		SERVER_UPDATE,

		/**
		 * Something unknown caused the websocket to close. The connection will be abandoned.
		 */
		ABNORMAL_CLOSE
	}
}

