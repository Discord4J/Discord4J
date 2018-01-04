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

/** A Discord voice channel. */
public interface VoiceChannel extends GuildChannel<VoiceChannel>, Channel {

	/**
	 * Gets the bitrate (in bits) for this voice channel.
	 *
	 * @return Gets the bitrate (in bits) for this voice channel.
	 */
	int getBitrate();

	/**
	 * Gets the user limit of this voice channel.
	 *
	 * @return The user limit of this voice channel.
	 */
	int getUserLimit();
}
