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
