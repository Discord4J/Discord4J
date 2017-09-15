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
package discord4j.common.pojo.embed;

/**
 * Represents an Embed Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#embed-object">Embed Object</a>
 */
public class EmbedPojo {

	private String title;
	private String type;
	private String description;
	private String url;
	private String timestamp;
	private int color;
	private EmbedFooterPojo footer;
	private EmbedImagePojo image;
	private EmbedThumbnailPojo thumbnail;
	private EmbedVideoPojo video;
	private EmbedProviderPojo provider;
	private EmbedAuthorPojo author;
	private EmbedFieldPojo[] fields;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public EmbedFooterPojo getFooter() {
		return footer;
	}

	public void setFooter(EmbedFooterPojo footer) {
		this.footer = footer;
	}

	public EmbedImagePojo getImage() {
		return image;
	}

	public void setImage(EmbedImagePojo image) {
		this.image = image;
	}

	public EmbedThumbnailPojo getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(EmbedThumbnailPojo thumbnail) {
		this.thumbnail = thumbnail;
	}

	public EmbedVideoPojo getVideo() {
		return video;
	}

	public void setVideo(EmbedVideoPojo video) {
		this.video = video;
	}

	public EmbedProviderPojo getProvider() {
		return provider;
	}

	public void setProvider(EmbedProviderPojo provider) {
		this.provider = provider;
	}

	public EmbedAuthorPojo getAuthor() {
		return author;
	}

	public void setAuthor(EmbedAuthorPojo author) {
		this.author = author;
	}

	public EmbedFieldPojo[] getFields() {
		return fields;
	}

	public void setFields(EmbedFieldPojo[] fields) {
		this.fields = fields;
	}
}
