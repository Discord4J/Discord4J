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

package sx.blah.discord.api.internal.json.requests;

/**
 * This is the request sent in order to edit a channel's information.
 */
public class ChannelEditRequest {

	public static class Builder {

		private String name;
		private Integer position;
		private String topic;

		/**
		 * Sets the new name of the channel.
		 *
		 * @param name the new name, must be 2-100 characters long.
		 * @return this builder, for chaining.
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the new position of the channel.
		 *
		 * @param position the new position.
		 * @return this builder, for chaining.
		 */
		public Builder position(int position) {
			this.position = position;
			return this;
		}

		/**
		 * Sets the new topic of the channel.
		 *
		 * @param topic the new topic
		 * @return this builder, for chaining.
		 */
		public Builder topic(String topic) {
			this.topic = topic;
			return this;
		}

		/**
		 * Builds the channel edit request.
		 *
		 * @return the channel edit request.
		 */
		public ChannelEditRequest build() {
			return new ChannelEditRequest(name, position, topic);
		}
	}

	private final String name;
	private final Integer position;
	private final String topic;

	ChannelEditRequest(String name, Integer position, String topic) {
		this.name = name;
		this.position = position;
		this.topic = topic;
	}

	public String getName() {
		return name;
	}

	public Integer getPosition() {
		return position;
	}

	public String getTopic() {
		return topic;
	}
}
