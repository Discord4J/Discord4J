package discord4j.core.`object`.entity

import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.spec.TextChannelEditSpec
import discord4j.core.spec.WebhookCreateSpec
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactor.asFlux

suspend fun TextChannel.update(spec: (TextChannelEditSpec) -> Unit): TextChannel = edit(spec).await()
suspend fun TextChannel.awaitBulkDelete(messages: ReceiveChannel<Snowflake>): List<Snowflake> =
    bulkDelete(messages.asFlux()).await()

suspend fun TextChannel.newWebhook(spec: (WebhookCreateSpec) -> Unit): Webhook = createWebhook(spec).await()
suspend fun TextChannel.awaitWebhooks(): List<Webhook> = webhooks.await()
