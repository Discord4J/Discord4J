package discord4j.core.`object`.entity

import discord4j.core.`object`.util.Image
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.grab

fun User.awaitAvatarUrl(format: Image.Format): String? = getAvatarUrl(format).grab()
suspend fun User.awaitAsMember(id: Snowflake): Member? = asMember(id).awaitNull()
suspend fun User.privateChannel(): PrivateChannel = privateChannel.await()
