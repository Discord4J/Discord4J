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

package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmbed;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Builds an EmbedObject for use in sending messages.
 *
 * @see IChannel#sendMessage(String, EmbedObject)
 * @see IChannel#sendFile(EmbedObject, File)
 * @see sx.blah.discord.handle.obj.IMessage#edit(String, EmbedObject)
 */
public class EmbedBuilder {

	/**
	 * The max amount of fields an embed can contain.
	 */
	public static final int FIELD_COUNT_LIMIT = 25;
	/**
	 * The max length of embed or field titles.
	 */
	public static final int TITLE_LENGTH_LIMIT = 256;
	/**
	 * The max length of field content.
	 */
	public static final int FIELD_CONTENT_LIMIT = 1024;
	/**
	 * The max length of embed descriptions.
	 */
	public static final int DESCRIPTION_CONTENT_LIMIT = 2048;
	/**
	 * The max length of footer text.
	 */
	public static final int FOOTER_CONTENT_LIMIT = DESCRIPTION_CONTENT_LIMIT;
	/**
	 * The maximum character limit across all visible fields.
	 */
	public static final int MAX_CHAR_LIMIT = 4000;

	private final EmbedObject embed = new EmbedObject(null, "rich", null, null, null, 0, null, null, null, null, null,
			null, null);
	private volatile List<EmbedObject.EmbedFieldObject> fields = new CopyOnWriteArrayList<>();
	private volatile Color color = new Color(0);

	/**
	 * If true, the builder will sanitize input to prevent errors automatically.
	 */
	private volatile boolean lenient = false;

	/**
	 * Create a new EmbedBuilder. Set what you want with the withX/appendX methods, and call {@link #build()}.
	 */
	public EmbedBuilder() {

	}

	/**
	 * This configures if the builder is lenient. When lenient, the builder will truncate strings in order to fit in an
	 * embed and ignore empty/null fields, otherwise will throw an IllegalArgumentException.
	 *
	 * @param lenient True to make the builder lenient, false for the opposite.
	 * @return Itself for chaining.
	 */
	public EmbedBuilder setLenient(boolean lenient) {
		this.lenient = lenient;

		return this;
	}

	/**
	 * Set the title of the embed.
	 *
	 * @param title The title
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTitle(String title) {
		if (title != null && title.trim().length() > TITLE_LENGTH_LIMIT)
			if (lenient)
				title = title.substring(0, TITLE_LENGTH_LIMIT);
			else
				throw new IllegalArgumentException(
						"Embed title cannot have more than " + TITLE_LENGTH_LIMIT + " characters");

		throwExceptionForCharacterLimit(title == null ? 4 : title.trim().length());

		embed.title = title;
		return this;
	}

	/**
	 * Set the description for the embed.
	 *
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder withDescription(String desc) {
		if (desc != null && desc.trim().length() > DESCRIPTION_CONTENT_LIMIT) {
			if (lenient)
				desc = desc.substring(0, DESCRIPTION_CONTENT_LIMIT);
			else
				throw new IllegalArgumentException(
						"Embed description cannot have more than " + DESCRIPTION_CONTENT_LIMIT + " characters");
		}

		throwExceptionForCharacterLimit(desc == null ? 4 : desc.trim().length());

		embed.description = desc;
		return this;
	}

	/**
	 * Set the description for the embed.
	 *
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder withDesc(String desc) {
		return withDescription(desc);
	}

	/**
	 * Appends to the description for the embed.
	 *
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder appendDescription(String desc) {
		if (embed.description == null)
			embed.description = "";
		if (desc != null && (embed.description + desc).trim().length() > DESCRIPTION_CONTENT_LIMIT) {
			if (lenient)
				desc = desc.substring(0, DESCRIPTION_CONTENT_LIMIT - embed.description.length());
			else
				throw new IllegalArgumentException(
						"Embed description cannot have more than " + DESCRIPTION_CONTENT_LIMIT + " characters");
		}

		throwExceptionForCharacterLimit(desc == null ? 4 : desc.trim().length());

		embed.description += desc;
		return this;
	}

	/**
	 * Appends to the description for the embed.
	 *
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder appendDesc(String desc) {
		return appendDescription(desc);
	}

	/**
	 * Set the timestamp for the embed. It is in the system's local time (and will be converted appropriately).
	 *
	 * @param ldt The local date-time
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTimestamp(LocalDateTime ldt) {
		embed.timestamp = ldt.atZone(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		return this;
	}

	/**
	 * Set the timestamp for the embed.
	 *
	 * @param millis The ms time
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTimestamp(long millis) {
		return withTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Z")));
	}

	/**
	 * Set the sidebar color with a Color object.
	 *
	 * @param color The color
	 * @return Itself for chaining
	 */
	public EmbedBuilder withColor(Color color) {
		this.color = color;
		return this;
	}

	/**
	 * Set the sidebar color with an RGB int.
	 *
	 * @param color The RGB int
	 * @return Itself for chaining
	 */
	public EmbedBuilder withColor(int color) {
		return withColor(new Color(color));
	}

	/**
	 * Set the sidebar color with bytes (0-255 inclusive) for red, green, and blue.
	 *
	 * @param r The red byte
	 * @param g The green byte
	 * @param b The blue byte
	 * @return Itself for chaining
	 */
	public EmbedBuilder withColor(int r, int g, int b) {
		return withColor(new Color(r, g, b));
	}

	/**
	 * Set the footer text (part on the bottom).
	 *
	 * @param footer The text
	 * @return Itself for chaining
	 */
	public EmbedBuilder withFooterText(String footer) {
		if (embed.footer == null)
			embed.footer = new EmbedObject.FooterObject(null, null, null);

		if (footer.trim().length() > FOOTER_CONTENT_LIMIT) {
			if (lenient)
				footer = footer.substring(0, FOOTER_CONTENT_LIMIT);
			else
				throw new IllegalArgumentException(
						"Embed footer text cannot have more than " + FOOTER_CONTENT_LIMIT + " characters");
		}

		throwExceptionForCharacterLimit(footer.trim().length());

		embed.footer.text = footer;
		return this;
	}

	/**
	 * Set the footer icon. You need footer text present for this to appear.
	 *
	 * @param iconUrl The icon URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withFooterIcon(String iconUrl) {
		if (embed.footer == null)
			embed.footer = new EmbedObject.FooterObject(null, null, null);

		embed.footer.icon_url = iconUrl;
		return this;
	}

	/**
	 * Set the image. If you're using this with
	 * {@link IChannel#sendFile(String, boolean, InputStream, String, EmbedObject)},
	 * you can specify this imageUrl as attachment://fileName.png to have the attachment image embedded. You can only
	 * use alphanumerics for the filename.
	 * <br>
	 * Only supported image types work.
	 *
	 * @param imageUrl The image URL
	 * @return Itself for chaining
	 * @see IChannel#sendFile(EmbedObject, File)
	 */
	public EmbedBuilder withImage(String imageUrl) {
		embed.image = new EmbedObject.ImageObject(imageUrl, null, 0, 0);
		return this;
	}

	/**
	 * Set the thumbnail (image displayed on the right).
	 *
	 * @param url The thumbnail URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withThumbnail(String url) {
		embed.thumbnail = new EmbedObject.ThumbnailObject(url, null, 0, 0);
		return this;
	}

	/**
	 * Set the author icon. Note that you need an author name for this to show up.
	 *
	 * @param url The icon URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withAuthorIcon(String url) {
		if (embed.author == null)
			embed.author = new EmbedObject.AuthorObject(null, null, null, null);

		embed.author.icon_url = url;
		return this;
	}

	/**
	 * Set the author's name.
	 *
	 * @param name The name
	 * @return Itself for chaining
	 */
	public EmbedBuilder withAuthorName(String name) {
		if (embed.author == null)
			embed.author = new EmbedObject.AuthorObject(null, null, null, null);

		throwExceptionForCharacterLimit(name.trim().length());

		embed.author.name = name;
		return this;
	}

	/**
	 * Set the author's URL. This is the link for when someone clicks the name. You need a name for this to be active.
	 *
	 * @param url The URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withAuthorUrl(String url) {
		if (embed.author == null)
			embed.author = new EmbedObject.AuthorObject(null, null, null, null);

		embed.author.url = url;
		return this;
	}

	/**
	 * Set the title's URL. This is the link for when someone clicks the title. You need a title for this to active.
	 *
	 * @param url The URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withUrl(String url) {
		embed.url = url;
		return this;
	}

	/**
	 * Sets the builder to ignore null/empty values passed in EmbedBuilder#appendField(). Useful if you don't want
	 * IllegalArgumentExceptions being thrown for that method.
	 *
	 * @return Itself for chaining
	 * @see #appendField(String, String, boolean)
	 * @deprecated See {@link #setLenient(boolean)}
	 */
	@Deprecated
	public EmbedBuilder ignoreNullEmptyFields() {
		lenient = true;
		return this;
	}

	/**
	 * Add a title-content field. Note: if a null or empty title or content is passed, this will throw an
	 * IllegalArgumentException. If you want the builder to safely ignore fields with null/empty values, use
	 * {@link #setLenient(boolean)}.
	 *
	 * @param title   The title
	 * @param content The content
	 * @param inline  If it should be inline (side-by-side)
	 * @return Itself for chaining
	 * @see #setLenient(boolean)
	 */
	public EmbedBuilder appendField(String title, String content, boolean inline) {
		if (((title == null || title.trim().isEmpty()) || (content == null || content.trim().isEmpty()))) {
			if (lenient)
				return this;
			throw new IllegalArgumentException("Title or content cannot be null/empty.");
		}

		if (fields.size() >= FIELD_COUNT_LIMIT) {
			if (lenient)
				fields = fields.subList(0, FIELD_COUNT_LIMIT);
			else
				throw new IllegalArgumentException("Embed cannot have more than " + FIELD_COUNT_LIMIT + " fields");
		}

		if (title.length() > TITLE_LENGTH_LIMIT) {
			if (lenient)
				title = title.substring(0, TITLE_LENGTH_LIMIT);
			else
				throw new IllegalArgumentException(
						"Embed field title cannot have more than " + TITLE_LENGTH_LIMIT + " characters");
		}

		if (content.length() > FIELD_CONTENT_LIMIT) {
			if (lenient)
				content = content.substring(0, FIELD_CONTENT_LIMIT);
			else
				throw new IllegalArgumentException(
						"Embed field content cannot have more than " + FIELD_CONTENT_LIMIT + " characters");
		}

		throwExceptionForCharacterLimit(title.trim().length() + content.trim().length());

		fields.add(new EmbedObject.EmbedFieldObject(title, content, inline));
		return this;
	}

	/**
	 * Copies the information of an IEmbedField and appends it onto the EmbedBuilder.
	 *
	 * @param field The field to copy
	 * @return Itself for chaining
	 * @see #setLenient(boolean)
	 */
	public EmbedBuilder appendField(IEmbed.IEmbedField field) {
		if (field == null) {
			if (lenient)
				return this;
			else
				throw new IllegalArgumentException("Field can not be null!");
		}
		return appendField(field.getName(), field.getValue(), field.isInline());
	}

	/**
	 * Clears the fields in the builder.
	 *
	 * @return Itself for chaining
	 */
	public EmbedBuilder clearFields(){
		fields.clear();
		return this;
	}

	/**
	 * Returns the number of fields in the builder.
	 *
	 * @return The number of fields in the builder.
	 * @see #FIELD_COUNT_LIMIT
	 */
	public int getFieldCount() {
		return fields.size();
	}

	/**
	 * Builds the EmbedObject.
	 *
	 * @return A newly built EmbedObject (calling this multiple times results in new objects)
	 */
	public EmbedObject build() {
		generateWarnings();
		throwExceptionForCharacterLimit(0);

		return new EmbedObject(embed.title, "rich", embed.description, embed.url, embed.timestamp, color == null
				? embed.color
				: ((color.getRed() & 0xFF) << 16) | ((color.getGreen() & 0xFF) << 8) | (color.getBlue() & 0xFF),
				embed.footer, embed.image, embed.thumbnail, embed.video, embed.provider, embed.author,
				fields.toArray(new EmbedObject.EmbedFieldObject[fields.size()]));
	}

	public int getTotalVisibleCharacters() {
		return (embed.title == null ? 0 : embed.title.length()) +
				(embed.description == null ? 0 : embed.description.length()) +
				(embed.footer == null ? 0 : (embed.footer.text == null ? 0 : embed.footer.text.length())) +
				(embed.author == null ? 0 : (embed.author.name == null ? 0 : embed.author.name.length())) +
				(fields.stream().mapToInt(efo -> (efo.name == null ? 0 : efo.name.length()) +
						(efo.value == null ? 0 : efo.value.length())).sum());
	}

	public boolean doesExceedCharacterLimit() {
		return getTotalVisibleCharacters() > MAX_CHAR_LIMIT;
	}

	private void throwExceptionForCharacterLimit(int extra) {
		if (extra < 0)
			throw new IllegalArgumentException("Extra cannot be negative!");
		if (!lenient && getTotalVisibleCharacters() + extra > MAX_CHAR_LIMIT)
			throw new IllegalArgumentException(
					"Embed " + (extra == 0 ? "exceeds" : "would exceed") + " character limit of " + MAX_CHAR_LIMIT +
							" (" + (extra == 0 ? "has" : "would have") + " " + (getTotalVisibleCharacters() + extra) +
							" chars)");
	}

	private void generateWarnings() {
		if (embed.footer != null) {
			// footer warnings
			if (embed.footer.icon_url != null && (embed.footer.text == null || embed.footer.text.isEmpty())) {
				Discord4J.LOGGER.warn(LogMarkers.UTIL,
						"Embed object warning - footer icon without footer text - footer icon will not be " +
								"visible");
			}
		}

		if (embed.author != null) {
			if (embed.author.name == null || embed.author.name.isEmpty()) {
				if (embed.author.icon_url != null) {
					Discord4J.LOGGER.warn(LogMarkers.UTIL,
							"Embed object warning - author icon without author name - author icon will not be " +
									"visible");
				}
				if (embed.author.url != null) {
					Discord4J.LOGGER.warn(LogMarkers.UTIL,
							"Embed object warning - author URL without author name - URL is useless and cannot" + " " +
									"be clicked");
				}
			}
		}
	}
}
