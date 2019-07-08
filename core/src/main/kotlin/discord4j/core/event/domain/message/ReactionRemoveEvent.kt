package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab


suspend fun ReactionRemoveEvent.awaitUser(): User = user.await()
suspend fun ReactionRemoveEvent.awaitChannel(): MessageChannel = channel.await()
suspend fun ReactionRemoveEvent.awaitMessage(): Message = message.await()
fun ReactionRemoveEvent.nullableGuildId(): Snowflake? = guildId.grab()
suspend fun ReactionRemoveEvent.awaitGuild(): Guild = guild.await()
