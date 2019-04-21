package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.await
import discord4j.core.grab


fun MessageCreateEvent.guildId(): Snowflake? = guildId.grab()
suspend fun MessageCreateEvent.guild(): Guild = guild.await()
fun MessageCreateEvent.member(): Member? = member.grab()
