package sx.blah.discord.util;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds an EmbedObject for use in sending messages.
 */
public class EmbedBuilder {

	private final EmbedObject embed = new EmbedObject(null, "rich", null, null, null, 0, null, null, null, null, null,
			null, null);
	private final List<EmbedObject.EmbedFieldObject> fields = new ArrayList<>();

	/**
	 * If true, this will not throw an IllegalArgumentException if you pass null/empty values to appendField.
	 */
	private boolean ignoreEmptyNullFields = false;

	public EmbedBuilder() {

	}

	/**
	 * Builds the EmbedObject.
	 * @return A newly built EmbedObject (calling this multiple times results in new objects)
	 */
	public EmbedObject build() {
		return new EmbedObject(embed.title, "rich", embed.description, embed.url, embed.timestamp, embed.color,
				embed.footer, embed.image, embed.thumbnail, embed.video, embed.provider, embed.author,
				fields.toArray(new EmbedObject.EmbedFieldObject[fields.size()]));
	}

	/**
	 * Set the title of the embed.
	 * @param title The title
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTitle(String title) {
		embed.title = title;
		return this;
	}

	/**
	 * Set the description for the embed.
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder withDescription(String desc) {
		embed.description = desc;
		return this;
	}

	/**
	 * Set the description for the embed.
	 * @param desc The description
	 * @return Itself for chaining
	 */
	public EmbedBuilder withDesc(String desc) {
		return withDescription(desc);
	}

	/**
	 * Set the timestamp for the embed. It is in the system's local time (and will be converted appropriately).
	 * @param ldt The local date-time
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTimestamp(LocalDateTime ldt) {
		embed.timestamp = ldt.atZone(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		return this;
	}

	/**
	 * Set the timestamp for the embed.
	 * @param millis The ms time
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTimestamp(long millis) {
		return withTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Z")));
	}

	/**
	 * Set the sidebar color with an object.
	 * @param color The color
	 * @return Itself for chaining
	 */
	public EmbedBuilder withColor(Color color) {
		return withColor(((color.getRed() & 0xFF) << 16) | ((color.getGreen() & 0xFF) << 8) | (color.getBlue() & 0xFF));
	}

	/**
	 * Set the sidebar color with an RGB int.
	 * @param color The RGB int
	 * @return Itself for chaining
	 */
	public EmbedBuilder withColor(int color) {
		embed.color = color;
		return this;
	}

	/**
	 * Set the sidebar color with bytes for red, green, and blue. The values are not clamped, and is up to the developer to keep betwen 0-255 (inclusive).
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
	 * @param footer The text
	 * @return Itself for chaining
	 */
	public EmbedBuilder withFooterText(String footer) {
		if (embed.footer == null)
			embed.footer = new EmbedObject.FooterObject(null, null, null);

		embed.footer.text = footer;
		return this;
	}

	/**
	 * Set the footer icon.
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
	 * Set the image.
	 * @param imageUrl The image URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withImage(String imageUrl) {
		embed.image = new EmbedObject.ImageObject(imageUrl, null, 0, 0);
		return this;
	}

	/**
	 * Set the thumbnail (image displayed on the right).
	 * @param url The thumbnail URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withThumbnail(String url) {
		embed.thumbnail = new EmbedObject.ThumbnailObject(url, null, 0, 0);
		return this;
	}

	/**
	 * Set the author icon.
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
	 * @param name The name
	 * @return Itself for chaining
	 */
	public EmbedBuilder withAuthorName(String name) {
		if (embed.author == null)
			embed.author = new EmbedObject.AuthorObject(null, null, null, null);

		embed.author.name = name;
		return this;
	}

	/**
	 * Set the author's URL. This is the link for when someone clicks the name.
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
	 * Set the title's URL. This is the link for when someone clicks the title.
	 * @param url The URL
	 * @return Itself for chaining
	 */
	public EmbedBuilder withUrl(String url) {
		embed.url = url;
		return this;
	}

	/**
	 * Sets the builder to ignore null/empty values passed in EmbedBuilder#appendField(). Useful if you don't want
	 * IllegalArgumentExceptions being thrown.
	 *
	 * @return Itself for chaining
	 * @see #appendField(String, String, boolean)
	 */
	public EmbedBuilder ignoreNullEmptyFields() {
		ignoreEmptyNullFields = true;
		return this;
	}

	/**
	 * Add a title-content field. Note: if a null or empty title or content is passed, this will throw an
	 * IllegalArgumentException. If you want the builder to safely ignore fields with null/empty values, use
	 * EmbedBuilder#ignoreEmptyNullFields().
	 * @param title The title
	 * @param content The content
	 * @param inline If it should be inline (side-by-side)
	 * @return Itself for chaining
	 * @see #ignoreNullEmptyFields()
	 */
	public EmbedBuilder appendField(String title, String content, boolean inline) {
		if (((title == null || title.isEmpty()) || (content == null || content.isEmpty()))) {
			if (ignoreEmptyNullFields)
				return this;
			throw new IllegalArgumentException("Title or content cannot be null/empty.");
		}

		fields.add(new EmbedObject.EmbedFieldObject(title, content, inline));
		return this;
	}

}
