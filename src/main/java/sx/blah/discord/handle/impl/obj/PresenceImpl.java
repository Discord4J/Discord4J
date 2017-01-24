package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

import java.util.Optional;

/**
 * Implementation of {@link sx.blah.discord.handle.obj.IPresence}
 */
public class PresenceImpl implements IPresence {

	private final Optional<String> playingText;
	private final Optional<String> streamingUrl;
	private final StatusType status;

	public PresenceImpl(Optional<String> playingText, Optional<String> streamingUrl, StatusType status) {
		this.playingText = playingText;
		this.streamingUrl = streamingUrl;
		this.status = status;
	}

	@Override
	public Optional<String> getPlayingText() {
		return playingText;
	}

	@Override
	public Optional<String> getStreamingUrl() {
		return streamingUrl;
	}

	@Override
	public StatusType getStatus() {
		return status;
	}

	@Override
	public IPresence copy() {
		return new PresenceImpl(playingText, streamingUrl, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PresenceImpl) {
			PresenceImpl other = (PresenceImpl) obj;

			return other.playingText.equals(playingText) && other.streamingUrl.equals(streamingUrl) &&
					other.status == status;
		}

		return super.equals(obj);
	}

	@Override
	public String toString() {
		return status + " - playing " + getPlayingText().orElse(null) + " with streaming URL " +
				getStreamingUrl().orElse(null);
	}
}
