package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.await


suspend fun MessageBulkDeleteEvent.awaitChannel(): MessageChannel = channel.await()
suspend fun MessageBulkDeleteEvent.awaitGuild(): Guild = guild.await()
