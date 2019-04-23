package discord4j.core.`object`.trait

import discord4j.core.`object`.ExtendedInvite
import discord4j.core.`object`.Invite
import discord4j.core.await
import discord4j.core.spec.InviteCreateSpec

/**
 * Requests to make a new invite.
 *
 * @spec A consumer that provides a "blank" [InviteCreateSpec] to be operated on.
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion,
 * the created [Invite] is returned. If an error is received it's thrown.
 */
suspend fun Invitable.newInvite(spec: (InviteCreateSpec) -> Unit): ExtendedInvite = createInvite(spec).await()

/**
 * Requests all the invites for this entity.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] that, upon successful completion,
 * the list of [Invite]s for this entity is returned. If an error is received it's thrown.
 */
suspend fun Invitable.invites(): List<Invite> = invites.await()
