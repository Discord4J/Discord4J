package discord4j.core.`object`

import discord4j.core.`object`.ExtendedPermissionOverwrite
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.GuildChannel
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.unit


/**
 * Requests the role that's associated with this overwrite.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful non-empty completion,
 * returns the [Role]. If the mono is empty it's null. If an error is received it's thrown.
 */
suspend fun ExtendedPermissionOverwrite.role(): Role? = role.awaitNull()

/**
 * Requests the user that's associated with this overwrite.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful non-empty completion,
 * returns the [User]. If the mono is empty it's null. If an error is received it's thrown.
 */
suspend fun ExtendedPermissionOverwrite.user(): User? = user.awaitNull()

/**
 * Requests the guild that's associated with this overwrite.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion, returns
 * the [Guild]. If an error is received it's thrown.
 */
suspend fun ExtendedPermissionOverwrite.guild(): Guild = guild.await()

/**
 * Requests the channel that's associated with this overwrite.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion, returns
 * the [GuildChannel]. If an error is received it's thrown.
 */
suspend fun ExtendedPermissionOverwrite.channel(): GuildChannel = channel.await()

/**
 * Requests to delete this overwrite.
 *
 * @param reason Optional reason.
 */
suspend fun ExtendedPermissionOverwrite.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
                
