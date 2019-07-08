package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab


suspend fun MessageUpdateEvent.awaitMessage(): Message = message.await()
suspend fun MessageUpdateEvent.awaitchannel(): MessageChannel = channel.await()
fun MessageUpdateEvent.nullableGuildId(): Snowflake? = guildId.grab()
suspend fun MessageUpdateEvent.awaitGuild(): Guild = guild.await()
fun MessageUpdateEvent.nullableOld(): Message? = old.grab()
fun MessageUpdateEvent.nullableCurrentContent(): String? = currentContent.grab()
