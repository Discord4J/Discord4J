package discord4j.core.event.domain.message

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab


fun MessageCreateEvent.nullableGuildId(): Snowflake? = guildId.grab()
suspend fun MessageCreateEvent.awaitGuild(): Guild = guild.await()
fun MessageCreateEvent.nullableMember(): Member? = member.grab()
