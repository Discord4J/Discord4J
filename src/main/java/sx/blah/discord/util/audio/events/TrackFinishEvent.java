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

import java.util.Optional;

/**
 * Dispatched when a track is finished playing.
 */
public class TrackFinishEvent extends AudioPlayerEvent {

	private final AudioPlayer.Track oldTrack, newTrack;

	public TrackFinishEvent(AudioPlayer player, AudioPlayer.Track oldTrack, AudioPlayer.Track newTrack) {
		super(player);
		this.oldTrack = oldTrack;
		this.newTrack = newTrack;
	}

	/**
	 * Gets the track that finished playing.
	 *
	 * @return The track that finished playing.
	 */
	public AudioPlayer.Track getOldTrack() {
		return oldTrack;
	}

	/**
	 * Gets the next track in the queue.
	 *
	 * @return The next track in the queue.
	 */
	public Optional<AudioPlayer.Track> getNewTrack() {
		return Optional.ofNullable(newTrack);
	}
}
