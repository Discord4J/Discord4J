package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user connects to a voice channel.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent} instead.
 */
@Deprecated
public class UserVoiceChannelJoinEvent extends sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent {
	
	public UserVoiceChannelJoinEvent(IVoiceChannel voiceChannel, IUser user) {
		super(voiceChannel, user);
	}
}
