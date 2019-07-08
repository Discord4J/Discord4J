package discord4j.core.event.domain.channel

import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.await


suspend fun TypingStartEvent.awaitChannel(): MessageChannel = channel.await()
suspend fun TypingStartEvent.awaitUser(): User = user.await()
