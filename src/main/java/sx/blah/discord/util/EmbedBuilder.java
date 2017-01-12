package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

	private final EmbedObject embed = new EmbedObject(null, "rich", null, null, null, 0, null, null, null, null, null,
			null, null);
	private final List<EmbedObject.EmbedFieldObject> fields = new ArrayList<>();
	private volatile Color color = new Color(0);

	/**
	 * If true, this will not throw an IllegalArgumentException if you pass null/empty values to appendField.
	 */
	private boolean ignoreEmptyNullFields = false;

	/**
	 * Create a new EmbedBuilder. Set what you want with the withX/appendX methods, and call {@link #build()}.
	 */
	public EmbedBuilder() {

	}

	/**
	 * Set the title of the embed.
	 *
	 * @param title The title
	 * @return Itself for chaining
	 */
	public EmbedBuilder withTitle(String title) {
		if (title.length() > TITLE_LENGTH_LIMIT)
			throw new IllegalArgumentException(
					"Embed title cannot have more than " + TITLE_LENGTH_LIMIT + " characters");

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
		if (desc.length() > DESCRIPTION_CONTENT_LIMIT)
			throw new IllegalArgumentException(
					"Embed description cannot have more than " + DESCRIPTION_CONTENT_LIMIT + " characters");

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
		if ((embed.description + desc).length() > DESCRIPTION_CONTENT_LIMIT)
			throw new IllegalArgumentException(
					"Embed description cannot have more than " + DESCRIPTION_CONTENT_LIMIT + " characters");

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

		if (footer.length() > FOOTER_CONTENT_LIMIT)
			throw new IllegalArgumentException(
					"Embed footer text cannot have more than " + FOOTER_CONTENT_LIMIT + " characters");

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
	 */
	public EmbedBuilder ignoreNullEmptyFields() {
		ignoreEmptyNullFields = true;
		return this;
	}

	/**
	 * Add a title-content field. Note: if a null or empty title or content is passed, this will throw an
	 * IllegalArgumentException. If you want the builder to safely ignore fields with null/empty values, use
	 * {@link #ignoreEmptyNullFields}.
	 *
	 * @param title   The title
	 * @param content The content
	 * @param inline  If it should be inline (side-by-side)
	 * @return Itself for chaining
	 * @see #ignoreNullEmptyFields()
	 */
	public EmbedBuilder appendField(String title, String content, boolean inline) {
		if (((title == null || title.isEmpty()) || (content == null || content.isEmpty()))) {
			if (ignoreEmptyNullFields)
				return this;
			throw new IllegalArgumentException("Title or content cannot be null/empty.");
		}

		if (fields.size() >= FIELD_COUNT_LIMIT)
			throw new IllegalArgumentException("Embed cannot have more than " + FIELD_COUNT_LIMIT + " fields");

		if (title.length() > TITLE_LENGTH_LIMIT)
			throw new IllegalArgumentException(
					"Embed field title cannot have more than " + TITLE_LENGTH_LIMIT + " characters");

		if (content.length() > FIELD_CONTENT_LIMIT)
			throw new IllegalArgumentException(
					"Embed field content cannot have more than " + FIELD_COUNT_LIMIT + " characters");

		fields.add(new EmbedObject.EmbedFieldObject(title, content, inline));
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

		return new EmbedObject(embed.title, "rich", embed.description, embed.url, embed.timestamp, color == null
				? embed.color
				: ((color.getRed() & 0xFF) << 16) | ((color.getGreen() & 0xFF) << 8) | (color.getBlue() & 0xFF),
				embed.footer, embed.image, embed.thumbnail, embed.video, embed.provider, embed.author,
				fields.toArray(new EmbedObject.EmbedFieldObject[fields.size()]));
	}

	private void generateWarnings() {
		if (embed.footer != null) {
			// footer warnings
			if (embed.footer.icon_url != null && (embed.footer.text == null || embed.footer.text.isEmpty())) {
				Discord4J.LOGGER
						.warn("Embed object warning - footer icon without footer text - footer icon will not be " +
								"visible");
			}
		}

		if (embed.author != null) {
			if (embed.author.name == null || embed.author.name.isEmpty()) {
				if (embed.author.icon_url != null) {
					Discord4J.LOGGER
							.warn("Embed object warning - author icon without author name - author icon will not be " +
									"visible");
				}
				if (embed.author.url != null) {
					Discord4J.LOGGER
							.warn("Embed object warning - author URL without author name - URL is useless and cannot" +
									" " +
									"be clicked");
				}
			}
		}
	}
}
