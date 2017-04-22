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
		return status + (getPlayingText().isPresent() ? " - playing " + getPlayingText().get() : "") +
				(getStreamingUrl().isPresent() ? " with streaming URL " + getStreamingUrl().get() : "");
	}
}
