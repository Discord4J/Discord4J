package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched when {@link AudioPlayer#clean()} is called on an {@link AudioPlayer} instance.
 */
public class AudioPlayerCleanEvent extends AudioPlayerEvent {

	public AudioPlayerCleanEvent(AudioPlayer player) {
		super(player);
	}
}
