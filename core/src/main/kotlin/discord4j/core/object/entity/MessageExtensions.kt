package discord4j.core.`object`.entity

import discord4j.core.`object`.entity.*
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.MessageEditSpec
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.grab
import discord4j.core.unit
import java.time.Instant

suspend fun Message.channel(): MessageChannel = channel.await()
fun Message.webhookId(): Snowflake? = webhookId.grab()
fun Message.author(): User? = author.grab()
suspend fun Message.authorAsMember(): Member? = authorAsMember.awaitNull()
fun Message.content(): String? = content.grab()
fun Message.editedTimestamp(): Instant? = editedTimestamp.grab()
suspend fun Message.userMentions(): List<User> = userMentions.await()
suspend fun Message.roleMentions(): List<Role> = roleMentions.await()
suspend fun Message.awaitReactors(emoji: ReactionEmoji): List<User> = getReactors(emoji).await()
suspend fun Message.webhook(): Webhook? = webhook.awaitNull()
suspend fun Message.guild(): Guild? = guild.awaitNull()
suspend fun Message.update(spec: (MessageEditSpec) -> Unit): Message = edit(spec).await()
suspend fun Message.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
suspend fun Message.awaitAddReaction(emoji: ReactionEmoji): Unit = addReaction(emoji).unit()
suspend fun Message.awaitRemoveReaction(emoji: ReactionEmoji, id: Snowflake): Unit = removeReaction(emoji, id).unit()
suspend fun Message.awaitRemoveSelfReaction(emoji: ReactionEmoji): Unit = removeSelfReaction(emoji).unit()
suspend fun Message.awaitRemoveAllReactions(): Unit = removeAllReactions().unit()
suspend fun Message.awaitUnpin(): Unit = unpin().unit()
