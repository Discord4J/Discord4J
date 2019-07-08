package discord4j.core.`object`.entity

import discord4j.core.await

suspend fun PrivateChannel.awaitRecipients(): List<User> = recipients.await()
