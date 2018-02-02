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

import discord4j.common.json.response.AttachmentResponse;
import discord4j.core.Client;
import discord4j.core.object.Snowflake;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * A Discord attachment.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#attachment-object">Attachment Object</a>
 */
public final class Attachment implements Entity {

	/** The Client associated to this object. */
	private final Client client;

	/** The raw data as represented by Discord. */
	private final AttachmentResponse attachment;

	/**
	 * Constructs an {@code Attachment} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param attachment The raw data as represented by Discord, must be non-null.
	 */
	public Attachment(final Client client, final AttachmentResponse attachment) {
		this.client = Objects.requireNonNull(client);
		this.attachment = Objects.requireNonNull(attachment);
	}

	@Override
	public Client getClient() {
		return client;
	}

	@Override
	public Snowflake getId() {
		return Snowflake.of(attachment.getId());
	}

	/**
	 * Gets the name of the file attached.
	 *
	 * @return The name of the file attached.
	 */
	public String getFilename() {
		return attachment.getFilename();
	}

	/**
	 * Gets the size of the file in bytes.
	 *
	 * @return The size of the file in bytes.
	 */
	public int getSize() {
		return attachment.getSize();
	}

	/**
	 * Gets the source URL of the file.
	 *
	 * @return The source URL of the file.
	 */
	public String getUrl() {
		return attachment.getUrl();
	}

	/**
	 * Gets a proxied URL of the file.
	 *
	 * @return A proxied URL of the file.
	 */
	public String getProxyUrl() {
		return attachment.getProxyUrl();
	}

	/**
	 * Gets the height of the file, if present.
	 *
	 * @return The height of the file, if present.
	 */
	public OptionalInt getHeight() {
		return Stream.of(attachment.getHeight())
				.filter(Objects::nonNull)
				.mapToInt(value -> value)
				.findFirst();
	}

	/**
	 * Gets the width of the file, if present.
	 *
	 * @return The width of the file, if present.
	 */
	public OptionalInt getWidth() {
		return Stream.of(attachment.getWidth())
				.filter(Objects::nonNull)
				.mapToInt(value -> value)
				.findFirst();
	}
}
