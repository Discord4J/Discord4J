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
 * This event is dispatched whenever a track is skipped.
 */
public class TrackSkipEvent extends AudioPlayerEvent {
	
	private final AudioPlayer.Track oldTrack;
	private final AudioPlayer.Track newTrack;
	
	public TrackSkipEvent(AudioPlayer player, AudioPlayer.Track oldTrack, AudioPlayer.Track newTrack) {
		super(player);
		this.oldTrack = oldTrack;
		this.newTrack = newTrack;
	}
	
	/**
	 * This gets the track that was skipped.
	 *
	 * @return The skipped track.
	 */
	public AudioPlayer.Track getTrack() {
		return oldTrack;
	}
	
	/**
	 * This gets the track that is now queued due to the previous track being skipped.
	 *
	 * @return The next track or null if there are none.
	 */
	public AudioPlayer.Track getNextTrack() {
		return newTrack;
	}
}
