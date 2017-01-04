package sx.blah.discord.handle.impl.events.guild.voice.user;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user connects to a voice channel.
 */
public class UserVoiceChannelJoinEvent extends UserVoiceChannelEvent {
	
	public UserVoiceChannelJoinEvent(IVoiceChannel voiceChannel, IUser user) {
		super(voiceChannel, user);
	}
}
