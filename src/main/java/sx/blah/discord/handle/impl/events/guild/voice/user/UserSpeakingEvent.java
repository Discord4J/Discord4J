package sx.blah.discord.handle.impl.events.guild.voice.user;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This is dispatched when a user starts or stops speaking
 */
public class UserSpeakingEvent extends UserVoiceChannelEvent {
	
	private final int ssrc;
    private final boolean speaking;

    public UserSpeakingEvent(IVoiceChannel channel, IUser user, int ssrc, boolean speaking) {
        super(channel, user);
        this.ssrc = ssrc;
        this.speaking = speaking;
    }

	/**
	 * Gets the ssrc-a unique number per user.
	 *
	 * @return The ssrc.
	 */
    public int getSsrc() {
        return ssrc;
    }

	/**
	 * Whether the user is now speaking or not.
	 *
	 * @return True if the user is speaking, false if otherwise.
	 */
    public boolean isSpeaking() {
        return speaking;
    }
}
