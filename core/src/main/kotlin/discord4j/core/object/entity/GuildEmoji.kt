package discord4j.core.`object`.entity

import discord4j.core.await
import discord4j.core.spec.GuildEmojiEditSpec
import discord4j.core.unit

suspend fun GuildEmoji.awaitRoles(): List<Role> = roles.await()
suspend fun GuildEmoji.awaitUser(): User = user.await()
suspend fun GuildEmoji.awaitGuild(): Guild = guild.await()
suspend fun GuildEmoji.update(spec: (GuildEmojiEditSpec) -> Unit): GuildEmoji = edit(spec).await()
suspend fun GuildEmoji.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
