package discord4j.core.`object`.entity

import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.grab
import discord4j.core.spec.BanQuerySpec
import discord4j.core.spec.GuildMemberEditSpec
import discord4j.core.unit

suspend fun Member.roles(): List<Role> = roles.await()
suspend fun Member.highestRole(): Role = highestRole.await()
suspend fun Member.guild(): Guild = guild.await()
fun Member.nickname(): String? = nickname.grab()
suspend fun Member.voiceState(): VoiceState? = voiceState.await()
suspend fun Member.presence(): Presence? = presence.await()
suspend fun Member.awaitKick(reason: String? = null): Unit = kick(reason).unit()
suspend fun Member.awaitBan(spec: (BanQuerySpec) -> Unit): Unit = ban(spec).unit()
suspend fun Member.awaitUnban(reason: String? = null): Unit = unban(reason).unit()
suspend fun Member.awaitAddRole(id: Snowflake, reason: String? = null): Unit = addRole(id, reason).unit()
suspend fun Member.awaitRemoveRole(id: Snowflake, reason: String? = null): Unit = removeRole(id, reason).unit()
suspend fun Member.basePermissions(): PermissionSet = basePermissions.await()
suspend fun Member.awaitIsHigher(other: Member): Boolean = awaitIsHigher(other.id)
suspend fun Member.awaitIsHigher(other: Snowflake): Boolean = isHigher(other).await()
suspend fun Member.awaitHasHigherRoles(other: Iterable<Role>): Boolean = hasHigherRoles(other).await()
suspend fun Member.update(spec: (GuildMemberEditSpec) -> Unit): Unit = edit(spec).unit()
