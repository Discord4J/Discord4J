package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

import java.util.Optional;

/**
 * This is dispatched when a track is finished playing.
 */
public class TrackFinishEvent extends AudioPlayerEvent {

	private final AudioPlayer.Track oldTrack, newTrack;

	public TrackFinishEvent(AudioPlayer player, AudioPlayer.Track oldTrack, AudioPlayer.Track newTrack) {
		super(player);
		this.oldTrack = oldTrack;
		this.newTrack = newTrack;
	}

	/**
	 * This gets the track that finished playing.
	 *
	 * @return The original track.
	 */
	public AudioPlayer.Track getOldTrack() {
		return oldTrack;
	}

	/**
	 * This gets the next track on the queue (if it exists).
	 *
	 * @return The (optional) next track.
	 */
	public Optional<AudioPlayer.Track> getNewTrack() {
		return Optional.ofNullable(newTrack);
	}
}
