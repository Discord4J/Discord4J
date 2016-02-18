package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IUser;

/**
 * This is dispatched when a user starts or stops speaking
 */
public class VoiceUserSpeaking extends Event {
    private final IUser user;
    private final int ssrc;
    private final boolean speaking;

    public VoiceUserSpeaking(IUser user, int ssrc, boolean speaking) {
        this.user = user;
        this.ssrc = ssrc;
        this.speaking = speaking;
    }

    public IUser getUser() {
        return user;
    }

    public int getSsrc() {
        return ssrc;
    }

    public boolean isSpeaking() {
        return speaking;
    }
}
