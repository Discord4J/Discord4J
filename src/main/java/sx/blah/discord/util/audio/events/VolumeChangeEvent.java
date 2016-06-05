package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This is dispatched when {@link AudioPlayer#setVolume(float)} is called.
 */
public class VolumeChangeEvent extends AudioPlayerEvent {

	private final float oldValue, newValue;

	public VolumeChangeEvent(AudioPlayer player, float oldValue, float newValue) {
		super(player);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * This gets the old volume for the player.
	 *
	 * @return The old volume.
	 */
	public float getOldValue() {
		return oldValue;
	}

	/**
	 * This gets the new volume for the player.
	 *
	 * @return The new volume.
	 */
	public float getNewValue() {
		return newValue;
	}
}
