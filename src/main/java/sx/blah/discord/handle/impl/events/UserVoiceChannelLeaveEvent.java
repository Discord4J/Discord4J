package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user disconnects from a voice channel.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent} instead.
 */
@Deprecated
public class UserVoiceChannelLeaveEvent extends sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent {
	
	public UserVoiceChannelLeaveEvent(IVoiceChannel voiceChannel, IUser user) {
		super(voiceChannel, user);
	}
}
