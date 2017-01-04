package sx.blah.discord.handle.impl.events.guild.voice.user;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user disconnects from a voice channel.
 */
public class UserVoiceChannelLeaveEvent extends UserVoiceChannelEvent {
	
	public UserVoiceChannelLeaveEvent(IVoiceChannel voiceChannel, IUser user) {
		super(voiceChannel, user);
	}
}
