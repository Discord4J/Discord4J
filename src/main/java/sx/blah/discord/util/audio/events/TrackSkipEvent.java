package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched whenever a track is skipped.
 */
public class TrackSkipEvent extends AudioPlayerEvent {
	
	private final AudioPlayer.Track track;
	
	public TrackSkipEvent(AudioPlayer player, AudioPlayer.Track track) {
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
