package discord4j.core.`object`.entity

import discord4j.core.unit

/**
 * Requests to delete the channel.
 *
 * @param reason The reason why this is being deleted.
 */
suspend fun Channel.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
