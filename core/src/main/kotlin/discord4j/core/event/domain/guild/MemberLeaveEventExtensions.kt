package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.guild.MemberLeaveEvent
import discord4j.core.await
import discord4j.core.grab


suspend fun MemberLeaveEvent.guild(): Guild = guild.await()
fun MemberLeaveEvent.member(): Member? = member.grab()
