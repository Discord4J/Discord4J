package discord4j.core.`object`.entity

import discord4j.core.grab

/**
 * Grabs the height of the attachment.
 *
 * @return The nullable height of the attachment.
 */
fun Attachment.nullableHeight(): Int? = height.grab()

/**
 * Grabs the width of the attachment.
 *
 * @return The nullable width of the attachment.
 */
fun Attachment.nullableWidth(): Int? = width.grab()
