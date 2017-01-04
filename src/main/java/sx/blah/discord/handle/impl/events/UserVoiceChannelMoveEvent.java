package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user moves from one voice channel to another.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent} instead.
 */
@Deprecated
public class UserVoiceChannelMoveEvent extends sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent {
	
	public UserVoiceChannelMoveEvent(IUser user, IVoiceChannel oldChannel, IVoiceChannel newChannel) {
		super(user, oldChannel, newChannel);
	}
}
