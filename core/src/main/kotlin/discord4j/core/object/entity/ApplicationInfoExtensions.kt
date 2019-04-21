package discord4j.core.`object`.entity

import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Image
import discord4j.core.await
import discord4j.core.grab

/**
 * Gets the icon for the application.
 *
 * @param format The format the icon is returned in. Must be PNG, JPG, or WEBP
 * @return The nullable icon for the application.
 */
fun ApplicationInfo.icon(format: Image.Format): String? = getIcon(format).grab()

/**
 * Gets the description for the application.
 *
 */
fun ApplicationInfo.description(): String? = description.grab()

/**
 * Requests the owner of the application.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion,
 * returns the [User] that represents the owner. If an error is received it's thrown.
 */
suspend fun ApplicationInfo.owner(): User = owner.await()
