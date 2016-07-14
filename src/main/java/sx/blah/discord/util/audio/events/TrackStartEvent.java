package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This is dispatched when a track starts playing.
 */
public class TrackStartEvent extends AudioPlayerEvent {

	private final AudioPlayer.Track track;

	public TrackStartEvent(AudioPlayer player, AudioPlayer.Track track) {
		super(player);
		this.track = track;
	}

	/**
	 * This gets the track that started playing.
	 *
	 * @return The track.
	 */
	public AudioPlayer.Track getTrack() {
		return track;
	}
}
