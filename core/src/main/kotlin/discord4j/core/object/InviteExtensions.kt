package discord4j.core.`object`

import discord4j.core.`object`.Invite
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.await
import discord4j.core.unit

/**
 * Requests the guild this invite is associated with.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion,
 * the [Guild] is returned. If an error is received it's thrown.
 */
suspend fun Invite.guild(): Guild = guild.await()

/**
 * Requests the channel this invite is associated with.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion,
 * the [TextChannel] is returned. If an error is received it's thrown.
 */
suspend fun Invite.channel(): TextChannel = channel.await()

/**
 * Requests to delete the invite.
 *
 * @param reason The nullable reason.
 */
suspend fun Invite.awaitDelete(reason: String? = null): Unit = delete(reason).unit()

