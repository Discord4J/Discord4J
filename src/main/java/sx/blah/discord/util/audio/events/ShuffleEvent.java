package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched when {@link AudioPlayer#shuffle()} is called.
 */
public class ShuffleEvent extends AudioPlayerEvent {

	public ShuffleEvent(AudioPlayer player) {
		super(player);
	}
}
