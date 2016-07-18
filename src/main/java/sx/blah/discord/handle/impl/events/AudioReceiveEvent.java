package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when audio is received.
 */
public class AudioReceiveEvent extends Event {

	private IUser user;
	private byte[] decodedAudio;

	public AudioReceiveEvent(IUser user, byte[] decodedAudio) {
		this.user = user;
		this.decodedAudio = decodedAudio;
	}

	public byte[] getDecodedAudio() {
		return this.decodedAudio;
	}

	public IUser getUser() {
		return this.user;
	}
}
