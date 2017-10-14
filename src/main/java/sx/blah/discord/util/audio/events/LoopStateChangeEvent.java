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
 * Dispatched when {@link AudioPlayer#setLoop(boolean)} is called.
 */
public class LoopStateChangeEvent extends AudioPlayerEvent {

	private final boolean newState;

	public LoopStateChangeEvent(AudioPlayer player, boolean newState) {
		super(player);
		this.newState = newState;
	}

	/**
	 * Gets the new loop state.
	 *
	 * @return The new loop state.
	 */
	public boolean getNewLoopState() {
		return newState;
	}

	/**
	 * Gets the old loop state.
	 *
	 * @return The old loop state.
	 */
	public boolean getOldLoopState() {
		return !newState;
	}
}
