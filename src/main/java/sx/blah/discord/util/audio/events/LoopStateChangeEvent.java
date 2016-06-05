package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is fired whenever {@link AudioPlayer#setLoop(boolean)} is called.
 */
public class LoopStateChangeEvent extends AudioPlayerEvent {

	private final boolean newState;

	public LoopStateChangeEvent(AudioPlayer player, boolean newState) {
		super(player);
		this.newState = newState;
	}

	/**
	 * This returns the new loop state.
	 *
	 * @return The new loop state.
	 */
	public boolean getNewLoopState() {
		return newState;
	}

	/**
	 * This returns the old loop state.
	 *
	 * @return The old loop state.
	 */
	public boolean getOldLoopState() {
		return !newState;
	}
}
