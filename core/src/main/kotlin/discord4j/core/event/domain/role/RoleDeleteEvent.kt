package discord4j.core.event.domain.role

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.await
import discord4j.core.grab


suspend fun RoleDeleteEvent.awaitGuild(): Guild = guild.await()
fun RoleDeleteEvent.nullableRole(): Role? = role.grab()
