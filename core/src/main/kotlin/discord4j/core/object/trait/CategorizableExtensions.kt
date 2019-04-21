package discord4j.core.`object`.trait

import discord4j.core.`object`.entity.Category
import discord4j.core.`object`.trait.Categorizable
import discord4j.core.`object`.util.Snowflake
import discord4j.core.awaitNull
import discord4j.core.grab

/**
 * Gets the ID of the category.
 *
 * @return The nullable [Snowflake] of the category.
 */
fun Categorizable.categoryId(): Snowflake? = categoryId.grab()

/**
 * Grabs the category related to this entity.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful non-empty
 * completion, returns the [Category]. If it's empty, it returns null. If an error is received it's thrown.
 */
suspend fun Categorizable.category(): Category? = category.awaitNull()
