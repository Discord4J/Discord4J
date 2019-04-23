package discord4j.core.`object`.entity

import discord4j.core.*
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.openSubscription
import kotlinx.coroutines.reactive.publish
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun MessageChannel.lastMessageId(): Snowflake? = lastMessageId.grab()
suspend fun MessageChannel.lastMessage(): Message? = lastMessage.awaitNull()
fun MessageChannel.lastPinTimestamp(): Instant? = lastPinTimestamp.grab()
suspend fun MessageChannel.newMessage(spec: (MessageCreateSpec) -> Unit): Message = createMessage(spec).await()
suspend fun MessageChannel.newMessage(message: String): Message = newMessage { it.setContent(message) }
suspend fun MessageChannel.newEmbed(spec: (EmbedCreateSpec) -> Unit): Message = newMessage { it.setEmbed(spec) }
suspend fun MessageChannel.awaitType(): Unit = type().unit()
suspend fun MessageChannel.awaitTypeUntil(trigger: suspend () -> Unit): ReceiveChannel<Long> = suspendCoroutine {
    it.resume(typeUntil(CoroutineScope(it.context).publish<Long> { trigger() }).openSubscription())
}

fun MessageChannel.awaitMessagesBefore(id: Snowflake): ReceiveChannel<Message> = getMessagesBefore(id).infinite()
fun MessageChannel.awaitMessagesAfter(id: Snowflake): ReceiveChannel<Message> = getMessagesAfter(id).infinite()
suspend fun MessageChannel.awaitMessage(id: Snowflake): Message? = getMessageById(id).awaitNull()
suspend fun MessageChannel.pinnedMessages(): List<Message> = pinnedMessages.await()
