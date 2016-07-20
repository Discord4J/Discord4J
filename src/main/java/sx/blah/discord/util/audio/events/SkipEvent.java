package sx.blah.discord.util.audio.events;

import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched whenever a track is skipped.
 * @deprecated Use {@link TrackSkipEvent} instead.
 */
@Deprecated
public class SkipEvent extends TrackSkipEvent {
	
	public SkipEvent(AudioPlayer player, AudioPlayer.Track track) {
		super(player, track);
	}
}
