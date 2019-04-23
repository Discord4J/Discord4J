package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab


suspend fun ReactionRemoveAllEvent.channel(): MessageChannel = channel.await()
suspend fun ReactionRemoveAllEvent.message(): Message = message.await()
fun ReactionRemoveAllEvent.guildId(): Snowflake? = guildId.grab()
suspend fun ReactionRemoveAllEvent.guild(): Guild = guild.await()
