package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IUser;

/**
 * This is dispatched when a user starts or stops speaking
 */
public class VoiceUserSpeakingEvent extends Event {
	/**
	 * The user involved
	 */
    private final IUser user;

    private final int ssrc;
    private final boolean speaking;

    public VoiceUserSpeakingEvent(IUser user, int ssrc, boolean speaking) {
        this.user = user;
        this.ssrc = ssrc;
        this.speaking = speaking;
    }

	/**
	 * Gets the user who started/ended speaking.
	 *
	 * @return The user.
	 */
    public IUser getUser() {
        return user;
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
