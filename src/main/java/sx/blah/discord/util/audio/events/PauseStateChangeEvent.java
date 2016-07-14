package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is fired whenever {@link AudioPlayer#setPaused(boolean)} is called.
 */
public class PauseStateChangeEvent extends AudioPlayerEvent {

	private final boolean newState;

	public PauseStateChangeEvent(AudioPlayer player, boolean newState) {
		super(player);
		this.newState = newState;
	}

	/**
	 * This returns the new pause state.
	 *
	 * @return The new pause state.
	 */
	public boolean getNewPauseState() {
		return newState;
	}

	/**
	 * This returns the old pause state.
	 *
	 * @return The old pause state.
	 */
	public boolean getOldPauseState() {
		return !newState;
	}
}
