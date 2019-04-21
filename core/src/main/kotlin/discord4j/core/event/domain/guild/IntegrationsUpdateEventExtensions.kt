package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.guild.IntegrationsUpdateEvent
import discord4j.core.await


suspend fun IntegrationsUpdateEvent.guild(): Guild = guild.await()
