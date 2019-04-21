package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.message.MessageBulkDeleteEvent
import discord4j.core.await


suspend fun MessageBulkDeleteEvent.channel(): MessageChannel = channel.await()
suspend fun MessageBulkDeleteEvent.guild(): Guild = guild.await()
