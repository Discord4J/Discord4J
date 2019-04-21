package discord4j.core.`object`.entity

import discord4j.core.*
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.publish
import java.time.Instant

fun MessageChannel.lastMessageId(): Snowflake? = lastMessageId.grab()
suspend fun MessageChannel.lastMessage(): Message? = lastMessage.awaitNull()
fun MessageChannel.lastPinTimestamp(): Instant? = lastPinTimestamp.grab()
suspend fun MessageChannel.newMessage(spec: (MessageCreateSpec) -> Unit): Message = createMessage(spec).await()
suspend fun MessageChannel.newMessage(message: String): Message = newMessage { it.setContent(message) }
suspend fun MessageChannel.newEmbed(spec: (EmbedCreateSpec) -> Unit): Message = newMessage { it.setEmbed(spec) }
suspend fun MessageChannel.awaitType(): Unit = type().unit()
suspend fun MessageChannel.awaitTypeUntil(trigger: suspend ProducerScope<*>.() -> Unit): ReceiveChannel<Long> =
    coroutineScope {
        typeUntil(publish<ReceiveChannel<Long>>(block = trigger)).infinite()
    }

suspend fun MessageChannel.awaitMessagesBefore(id: Snowflake): List<Message> = getMessagesBefore(id).await()
suspend fun MessageChannel.awaitMessagesAfter(id: Snowflake): List<Message> = getMessagesAfter(id).await()
suspend fun MessageChannel.awaitMessage(id: Snowflake): Message? = getMessageById(id).awaitNull()
suspend fun MessageChannel.pinnedMessages(): List<Message> = pinnedMessages.await()
