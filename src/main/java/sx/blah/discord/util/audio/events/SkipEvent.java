package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched whenever a track is skipped.
 */
public class SkipEvent extends AudioPlayerEvent {

	private final AudioPlayer.Track track;

	public SkipEvent(AudioPlayer player, AudioPlayer.Track track) {
		super(player);
		this.track = track;
	}

	/**
	 * This gets the track that was skipped.
	 *
	 * @return The skipped track.
	 */
	public AudioPlayer.Track getTrack() {
		return track;
	}
}
