package sx.blah.discord.util.audio.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This represents an event which is dispatched by an {@link sx.blah.discord.util.audio.AudioPlayer}.
 */
public class AudioPlayerEvent extends Event {

	protected final AudioPlayer player;

	public AudioPlayerEvent(AudioPlayer player) {
		this.player = player;
	}

	/**
	 * This gets the {@link AudioPlayer} instance which dispatched this event.
	 *
	 * @return The player.
	 */
	public AudioPlayer getPlayer() {
		return player;
	}
}
