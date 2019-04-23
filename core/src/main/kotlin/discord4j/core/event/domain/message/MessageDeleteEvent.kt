package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.await
import discord4j.core.grab


fun MessageDeleteEvent.message(): Message? = message.grab()
suspend fun MessageDeleteEvent.channel(): MessageChannel = channel.await()
