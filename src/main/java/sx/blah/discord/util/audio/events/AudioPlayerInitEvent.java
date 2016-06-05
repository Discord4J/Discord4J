package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched when an {@link AudioPlayer} instance is initialized.
 */
public class AudioPlayerInitEvent extends AudioPlayerEvent {

	public AudioPlayerInitEvent(AudioPlayer player) {
		super(player);
	}
}
