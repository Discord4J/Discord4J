package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.guild.MemberUpdateEvent
import discord4j.core.await
import discord4j.core.grab


suspend fun MemberUpdateEvent.guild(): Guild = guild.await()
suspend fun MemberUpdateEvent.member(): Member = member.await()
fun MemberUpdateEvent.old(): Member? = old.grab()
fun MemberUpdateEvent.currentNickname(): String? = currentNickname.grab()
