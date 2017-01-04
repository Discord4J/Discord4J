package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This is dispatched when a user starts or stops speaking
 * @deprecated Use {@link UserSpeakingEvent} instead.
 */
@Deprecated
public class VoiceUserSpeakingEvent extends UserSpeakingEvent {
	
	public VoiceUserSpeakingEvent(IVoiceChannel channel, IUser user, int ssrc, boolean speaking) {
		super(channel, user, ssrc, speaking);
	}
}
