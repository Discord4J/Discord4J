package discord4j.core.`object`.entity

import discord4j.core.`object`.entity.Category
import discord4j.core.`object`.entity.GuildChannel
import discord4j.core.spec.CategoryEditSpec
import discord4j.core.await

/**
 * Requests the channels associated with this [Category].
 *
 * @return A suspended call to the ='[reactor.core.publisher.Mono] that, upon successful completion,
 * returns the [List] of [GuildChannel]s. If an error is received it's thrown.
 */
suspend fun Category.channels(): List<GuildChannel> = channels.await()

/**
 * Requests to update the current category.
 *
 * @param spec A consumer that provides a "blank" [CategoryEditSpec] to be operated on.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [Category.edit] that, upon
 * successful completion, returns the edited [Category]. If an error is received, it's thrown.
 */
suspend fun Category.update(spec: (CategoryEditSpec) -> Unit): Category  = edit(spec).await()
