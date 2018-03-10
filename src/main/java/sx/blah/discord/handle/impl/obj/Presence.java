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

import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

import java.util.Objects;
import java.util.Optional;

/**
 * The default implementation of {@link IPresence}.
 */
public class Presence implements IPresence {
	/**
	 * The nullable text of the presence.
	 */
	private final String text;
	/**
	 * The nullable streaming url of the presence.
	 */
	private final String streamingUrl;
	/**
	 * The type of status of the presence.
	 */
	private final StatusType status;
	/**
	 * The activity of this presence.
	 */
	private ActivityType activity;

	public Presence(String text, String streamingUrl, StatusType status, ActivityType activity) {
		this.text = text;
		this.streamingUrl = streamingUrl;
		this.status = status;
		this.activity = activity;
	}

	@Override
	public Optional<String> getText() {
		return Optional.ofNullable(text);
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
		return new Presence(text, streamingUrl, status, activity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IPresence)) return false;

		IPresence other = (IPresence) obj;
		return Objects.equals(other.getText(), getText())
				&& Objects.equals(other.getStreamingUrl(), getStreamingUrl())
				&& Objects.equals(other.getStatus(), getStatus())
				&& Objects.equals(other.getActivity(), getActivity());
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, streamingUrl, status, activity);
	}

	@Override
	public String toString() {
		return "Presence(" + text + " : " + streamingUrl + " : " + status.name() + " : " + activity.name() + ")";
	}

	@Override
	public Optional<ActivityType> getActivity() {
		return Optional.ofNullable(activity);
	}
}
