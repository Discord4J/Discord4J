package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab


suspend fun ReactionAddEvent.awaitUser(): User = user.await()
suspend fun ReactionAddEvent.awaitChannel(): MessageChannel = channel.await()
suspend fun ReactionAddEvent.awaitMessage(): Message = message.await()
fun ReactionAddEvent.nullableGuildId(): Snowflake? = guildId.grab()
suspend fun ReactionAddEvent.awaitGuild(): Guild = guild.await()
