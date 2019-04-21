package discord4j.core.event.domain

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.PresenceUpdateEvent
import discord4j.core.await
import discord4j.core.grab


suspend fun PresenceUpdateEvent.guild(): Guild = guild.await()
fun PresenceUpdateEvent.oldUser(): User? = oldUser.grab()
fun PresenceUpdateEvent.newUsername(): String? = newUsername.grab()
fun PresenceUpdateEvent.newDiscriminator(): String? = newDiscriminator.grab()
fun PresenceUpdateEvent.newAvatar(): String? = newAvatar.grab()
suspend fun PresenceUpdateEvent.user(): User = user.await()
suspend fun PresenceUpdateEvent.member(): Member = member.await()
fun PresenceUpdateEvent.old(): Presence? = old.grab()
