package discord4j.core.`object`.entity

import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.grab
import discord4j.core.spec.MessageEditSpec
import discord4j.core.unit
import java.time.Instant

suspend fun Message.awaitChannel(): MessageChannel = channel.await()
fun Message.nullableWebhookId(): Snowflake? = webhookId.grab()
fun Message.nullableAuthor(): User? = author.grab()
suspend fun Message.awaitAuthorAsMember(): Member? = authorAsMember.awaitNull()
fun Message.nullableContent(): String? = content.grab()
fun Message.nullableEditedTimestamp(): Instant? = editedTimestamp.grab()
suspend fun Message.awaitUserMentions(): List<User> = userMentions.await()
suspend fun Message.awaitRoleMentions(): List<Role> = roleMentions.await()
suspend fun Message.awaitReactors(emoji: ReactionEmoji): List<User> = getReactors(emoji).await()
suspend fun Message.awaitWebhook(): Webhook? = webhook.awaitNull()
suspend fun Message.awaitGuild(): Guild? = guild.awaitNull()
suspend fun Message.update(spec: (MessageEditSpec) -> Unit): Message = edit(spec).await()
suspend fun Message.awaitDelete(reason: String? = null): Unit = delete(reason).unit()
suspend fun Message.awaitAddReaction(emoji: ReactionEmoji): Unit = addReaction(emoji).unit()
suspend fun Message.awaitRemoveReaction(emoji: ReactionEmoji, id: Snowflake): Unit = removeReaction(emoji, id).unit()
suspend fun Message.awaitRemoveSelfReaction(emoji: ReactionEmoji): Unit = removeSelfReaction(emoji).unit()
suspend fun Message.awaitRemoveAllReactions(): Unit = removeAllReactions().unit()
suspend fun Message.awaitUnpin(): Unit = unpin().unit()
