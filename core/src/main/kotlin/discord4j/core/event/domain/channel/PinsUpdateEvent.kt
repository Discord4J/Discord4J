package discord4j.core.event.domain.channel

import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.await
import discord4j.core.grab
import java.time.Instant


suspend fun PinsUpdateEvent.awaitChannel(): MessageChannel = channel.await()
fun PinsUpdateEvent.nullableLastPinTimestamp(): Instant? = lastPinTimestamp.grab()
