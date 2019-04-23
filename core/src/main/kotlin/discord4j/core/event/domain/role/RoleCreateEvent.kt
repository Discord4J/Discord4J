package discord4j.core.event.domain.role

import discord4j.core.`object`.entity.Guild
import discord4j.core.await


suspend fun RoleCreateEvent.guild(): Guild = guild.await()
