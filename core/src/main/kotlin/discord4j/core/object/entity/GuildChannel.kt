package discord4j.core.`object`.entity

import discord4j.core.`object`.ExtendedPermissionOverwrite
import discord4j.core.`object`.PermissionOverwrite
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.grab
import discord4j.core.unit

suspend fun GuildChannel.awaitGuild(): Guild = guild.await()
fun GuildChannel.awaitOverwriteForMember(id: Snowflake): ExtendedPermissionOverwrite? = getOverwriteForMember(id).grab()
fun GuildChannel.awaitOverwriteForRole(id: Snowflake): ExtendedPermissionOverwrite? = getOverwriteForRole(id).grab()
suspend fun GuildChannel.awaitEffectivePermissions(id: Snowflake): PermissionSet? =
    getEffectivePermissions(id).awaitNull()

suspend fun GuildChannel.awaitPosition(): Int = position.await()
suspend fun GuildChannel.awaitAddMemberOverwrite(
    id: Snowflake,
    overwrite: PermissionOverwrite,
    reason: String? = null
): Unit = addMemberOverwrite(id, overwrite, reason).unit()

suspend fun GuildChannel.awaitAddRoleOverwrite(
    id: Snowflake,
    overwrite: PermissionOverwrite,
    reason: String? = null
): Unit = addRoleOverwrite(id, overwrite, reason).unit()
