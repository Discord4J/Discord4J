package discord4j.core.`object`

import java.awt.Color
import java.time.Instant

/**
 * Gets the title of the embed.
 *
 * @return The nullable title of the embed.
 */
fun Embed.title(): String? = title.orElse(null)

/**
 * Gets the description of the embed.
 *
 * @return The nullable description of the embed.
 */
fun Embed.description(): String? = description.orElse(null)

/**
 * Gets the url of the embed.
 *
 * @return The nullable url of the embed.
 */
fun Embed.url(): String? = url.orElse(null)

/**
 * Gets the timestamp of the embed.
 *
 * @return The nullable timestamp of the embed.
 */
fun Embed.timestamp(): Instant? = timestamp.orElse(null)

/**
 * Gets the color of the embed.
 *
 * @return The nullable color of the embed.
 */
fun Embed.color(): Color? = color.orElse(null)

/**
 * Gets the footer of the embed.
 *
 * @return The nullable footer of the embed.
 */
fun Embed.footer(): Embed.Footer? = footer.orElse(null)

/**
 * Gets the image of the embed.
 *
 * @return The nullable image of the embed.
 */
fun Embed.image(): Embed.Image? = image.orElse(null)

/**
 * Gets the thumbnail of the embed.
 *
 * @return The nullable thumbnail of the embed.
 */
fun Embed.thumbnail(): Embed.Thumbnail? = thumbnail.orElse(null)

/**
 * Gets the video of the embed.
 *
 * @return The nullable video of the embed.
 */
fun Embed.video(): Embed.Video? = video.orElse(null)

/**
 * Gets the provider of the embed.
 *
 * @return The nullable provider of the embed.
 */
fun Embed.provider(): Embed.Provider? = provider.orElse(null)

/**
 * Gets the author of the embed.
 *
 * @return The nullable author of the embed.
 */
fun Embed.author(): Embed.Author? = author.orElse(null)
