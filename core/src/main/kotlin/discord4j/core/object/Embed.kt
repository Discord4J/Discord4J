package discord4j.core.`object`

import java.awt.Color
import java.time.Instant

/**
 * Gets the title of the embed.
 *
 * @return The nullable title of the embed.
 */
fun Embed.nullableTitle(): String? = title.orElse(null)

/**
 * Gets the description of the embed.
 *
 * @return The nullable description of the embed.
 */
fun Embed.nullableDescription(): String? = description.orElse(null)

/**
 * Gets the url of the embed.
 *
 * @return The nullable url of the embed.
 */
fun Embed.nullableUrl(): String? = url.orElse(null)

/**
 * Gets the timestamp of the embed.
 *
 * @return The nullable timestamp of the embed.
 */
fun Embed.nullableTimestamp(): Instant? = timestamp.orElse(null)

/**
 * Gets the color of the embed.
 *
 * @return The nullable color of the embed.
 */
fun Embed.nullableColor(): Color? = color.orElse(null)

/**
 * Gets the footer of the embed.
 *
 * @return The nullable footer of the embed.
 */
fun Embed.nullableFooter(): Embed.Footer? = footer.orElse(null)

/**
 * Gets the image of the embed.
 *
 * @return The nullable image of the embed.
 */
fun Embed.nullableImage(): Embed.Image? = image.orElse(null)

/**
 * Gets the thumbnail of the embed.
 *
 * @return The nullable thumbnail of the embed.
 */
fun Embed.nullableThumbnail(): Embed.Thumbnail? = thumbnail.orElse(null)

/**
 * Gets the video of the embed.
 *
 * @return The nullable video of the embed.
 */
fun Embed.nullableVideo(): Embed.Video? = video.orElse(null)

/**
 * Gets the provider of the embed.
 *
 * @return The nullable provider of the embed.
 */
fun Embed.nullableProvider(): Embed.Provider? = provider.orElse(null)

/**
 * Gets the author of the embed.
 *
 * @return The nullable author of the embed.
 */
fun Embed.nullableAuthor(): Embed.Author? = author.orElse(null)
