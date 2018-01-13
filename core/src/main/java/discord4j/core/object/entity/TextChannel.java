/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

/** A Discord text channel. */
public interface TextChannel extends GuildChannel, MessageChannel {

	/**
	 * Gets the channel topic.
	 *
	 * @return The channel topic.
	 */
	String getTopic();

	/**
	 * Gets whether this channel is considered NSFW (Not Safe For Work).
	 *
	 * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
	 */
	boolean isNsfw();

	/**
	 * Gets the <i>raw</i> mention. This is the format utilized to directly mention another text channel (assuming the
	 * text channel exists in context of the mention).
	 *
	 * @return The <i>raw</i> mention.
	 */
	default String getMention() {
		return "<#" + getId().asString() + ">";
	}
}
