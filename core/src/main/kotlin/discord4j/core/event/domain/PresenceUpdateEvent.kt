package discord4j.core.event.domain

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.Presence
import discord4j.core.await
import discord4j.core.grab


suspend fun PresenceUpdateEvent.awaitGuild(): Guild = guild.await()
fun PresenceUpdateEvent.nullableOldUser(): User? = oldUser.grab()
fun PresenceUpdateEvent.nullableNewUsername(): String? = newUsername.grab()
fun PresenceUpdateEvent.nullableNewDiscriminator(): String? = newDiscriminator.grab()
fun PresenceUpdateEvent.nullableNewAvatar(): String? = newAvatar.grab()
suspend fun PresenceUpdateEvent.awaitUser(): User = user.await()
suspend fun PresenceUpdateEvent.awaitMember(): Member = member.await()
fun PresenceUpdateEvent.nullableOld(): Presence? = old.grab()
