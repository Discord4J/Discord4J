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
package discord4j.common.json.request;

import discord4j.common.json.EmbedFieldEntity;

import java.util.Arrays;

public class EmbedRequest {

	private final String title;
	private final String description;
	private final String url;
	private final String timestamp;
	private final int color;
	private final EmbedFooterRequest footer;
	private final EmbedImageRequest image;
	private final EmbedThumbnailRequest thumbnail;
	private final EmbedAuthorRequest author;
	private final EmbedFieldEntity[] fields;

	public EmbedRequest(String title, String description, String url, String timestamp, int color,
	                    EmbedFooterRequest footer, EmbedImageRequest image,
	                    EmbedThumbnailRequest thumbnail, EmbedAuthorRequest author,
	                    EmbedFieldEntity[] fields) {
		this.title = title;
		this.description = description;
		this.url = url;
		this.timestamp = timestamp;
		this.color = color;
		this.footer = footer;
		this.image = image;
		this.thumbnail = thumbnail;
		this.author = author;
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "EmbedRequest[" +
				"title=" + title +
				", description=" + description +
				", url=" + url +
				", timestamp=" + timestamp +
				", color=" + color +
				", footer=" + footer +
				", image=" + image +
				", thumbnail=" + thumbnail +
				", author=" + author +
				", fields=" + Arrays.toString(fields) +
				']';
	}
}
