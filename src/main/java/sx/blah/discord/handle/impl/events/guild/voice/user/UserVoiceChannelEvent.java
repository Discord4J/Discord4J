package sx.blah.discord.handle.impl.events.guild.voice.user;

import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This represents a generic voice channel event involving a user.
 */
public abstract class UserVoiceChannelEvent extends VoiceChannelEvent {
	
	private final IUser user;
	
	public UserVoiceChannelEvent(IVoiceChannel voiceChannel, IUser user) {
		super(voiceChannel);
		this.user = user;
	}
	
	/**
	 * This gets the user involved in this event.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}
}
