package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.await
import discord4j.core.grab


suspend fun ReactionAddEvent.user(): User = user.await()
suspend fun ReactionAddEvent.channel(): MessageChannel = channel.await()
suspend fun ReactionAddEvent.message(): Message = message.await()
fun ReactionAddEvent.guildId(): Snowflake? = guildId.grab()
suspend fun ReactionAddEvent.guild(): Guild = guild.await()
