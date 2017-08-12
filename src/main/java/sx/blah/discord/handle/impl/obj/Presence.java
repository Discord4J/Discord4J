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

import java.util.Objects;
import java.util.Optional;

/**
 * The default implementation of {@link IPresence}.
 */
public class Presence implements IPresence {

	/**
	 * The nullable playing text of the presence.
	 */
	private final String playingText;
	/**
	 * The nullable streaming url of the presence.
	 */
	private final String streamingUrl;
	/**
	 * The type of status of the presence.
	 */
	private final StatusType status;

	public Presence(String playingText, String streamingUrl, StatusType status) {
		this.playingText = playingText;
		this.streamingUrl = streamingUrl;
		this.status = status;
	}

	@Override
	public Optional<String> getPlayingText() {
		return Optional.ofNullable(playingText);
	}

	@Override
	public Optional<String> getStreamingUrl() {
		return Optional.ofNullable(streamingUrl);
	}

	@Override
	public StatusType getStatus() {
		return status;
	}

	@Override
	public IPresence copy() {
		return new Presence(playingText, streamingUrl, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (!Presence.class.isAssignableFrom(obj.getClass())) return false;

		Presence other = (Presence) obj;
		return Objects.equals(other.playingText, this.playingText)
				&& Objects.equals(other.streamingUrl, this.streamingUrl)
				&& other.status == this.status;
	}

	@Override
	public String toString() {
		return status + (getPlayingText().isPresent() ? " - playing " + getPlayingText().get() : "") +
				(getStreamingUrl().isPresent() ? " with streaming URL " + getStreamingUrl().get() : "");
	}
}
