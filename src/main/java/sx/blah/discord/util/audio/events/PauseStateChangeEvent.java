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
 * Dispatched when {@link AudioPlayer#setPaused(boolean)} is called.
 */
public class PauseStateChangeEvent extends AudioPlayerEvent {

	private final boolean newState;

	public PauseStateChangeEvent(AudioPlayer player, boolean newState) {
		super(player);
		this.newState = newState;
	}

	/**
	 * Gets the new pause state.
	 *
	 * @return The new pause state.
	 */
	public boolean getNewPauseState() {
		return newState;
	}

	/**
	 * Gets the old pause state.
	 *
	 * @return The old pause state.
	 */
	public boolean getOldPauseState() {
		return !newState;
	}
}
