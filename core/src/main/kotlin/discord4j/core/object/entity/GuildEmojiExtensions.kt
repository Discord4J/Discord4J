package discord4j.core.`object`.entity

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.GuildEmoji
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.spec.GuildEmojiEditSpec
import discord4j.core.await
import discord4j.core.unit

suspend fun GuildEmoji.roles(): List<Role> = roles.await()
suspend fun GuildEmoji.user(): User = user.await()
suspend fun GuildEmoji.guild(): Guild = guild.await()
suspend fun GuildEmoji.update(spec: (GuildEmojiEditSpec) -> Unit): GuildEmoji = edit(spec).await()
suspend fun GuildEmoji.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
