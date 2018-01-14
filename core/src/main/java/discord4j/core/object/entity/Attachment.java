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

import java.util.OptionalInt;

/** Represents an attachment residing in a message. */
public interface Attachment extends Entity {

	/**
	 * Gets the name of the file attached.
	 *
	 * @return The name of the file attached.
	 */
	String getFilename();

	/**
	 * Gets the size of the file in bytes.
	 *
	 * @return The size of the file in bytes.
	 */
	int getSize();

	/**
	 * Gets the source URL of the file.
	 *
	 * @return The source URL of the file.
	 */
	String getUrl();

	/**
	 * Gets a proxied URL of the file.
	 *
	 * @return A proxied URL of the file.
	 */
	String getProxyUrl();

	/**
	 * Gets the height of the file, if present.
	 *
	 * @return The height of the file, if present.
	 */
	OptionalInt getHeight();

	/**
	 * Gets the width of the file, if present.
	 *
	 * @return The width of the file, if present.
	 */
	OptionalInt getWidth();
}
