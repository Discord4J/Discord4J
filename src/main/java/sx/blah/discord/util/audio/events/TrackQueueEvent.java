package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This is dispatched whenever a track is queued.
 */
public class TrackQueueEvent extends AudioPlayerEvent {

	private final AudioPlayer.Track track;

	public TrackQueueEvent(AudioPlayer player, AudioPlayer.Track track) {
		super(player);
		this.track = track;
	}

	/**
	 * This gets the {@link sx.blah.discord.util.audio.AudioPlayer.Track} instance queued.
	 *
	 * @return The track.
	 */
	public AudioPlayer.Track getTrack() {
		return track;
	}
}
