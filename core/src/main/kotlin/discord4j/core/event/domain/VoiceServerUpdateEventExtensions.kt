package discord4j.core.event.domain

import discord4j.core.`object`.entity.Guild
import discord4j.core.await


suspend fun VoiceServerUpdateEvent.guild(): Guild = guild.await()
