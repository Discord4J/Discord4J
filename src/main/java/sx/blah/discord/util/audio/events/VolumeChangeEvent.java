/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * Dispatched when {@link AudioPlayer#setVolume(float)} is called.
 */
public class VolumeChangeEvent extends AudioPlayerEvent {

	private final float oldValue, newValue;

	public VolumeChangeEvent(AudioPlayer player, float oldValue, float newValue) {
		super(player);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Gets the old volume of the player.
	 *
	 * @return The old volume.
	 */
	public float getOldValue() {
		return oldValue;
	}

	/**
	 * Gets the new volume of the player.
	 *
	 * @return The new volume.
	 */
	public float getNewValue() {
		return newValue;
	}
}
