package discord4j.core.`object`.entity

import discord4j.core.await
import discord4j.core.grab
import discord4j.core.spec.WebhookEditSpec
import discord4j.core.unit

suspend fun Webhook.awaitGuild(): Guild = guild.await()
suspend fun Webhook.awaitChannel(): TextChannel = channel.await()
suspend fun Webhook.awaitCreator(): User = creator.await()
fun Webhook.nullableN(): String? = name.grab()
fun Webhook.nullableAvatar(): String? = avatar.grab()
suspend fun Webhook.awaitDelete(): Unit = delete().unit()
suspend fun Webhook.update(spec: (WebhookEditSpec) -> Unit): Webhook = edit(spec).await()
