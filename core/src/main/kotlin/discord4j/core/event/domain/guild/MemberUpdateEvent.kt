package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.await
import discord4j.core.grab


suspend fun MemberUpdateEvent.awaitGuild(): Guild = guild.await()
suspend fun MemberUpdateEvent.awaitMember(): Member = member.await()
fun MemberUpdateEvent.nullableOld(): Member? = old.grab()
fun MemberUpdateEvent.nullableCurrentNickname(): String? = currentNickname.grab()
