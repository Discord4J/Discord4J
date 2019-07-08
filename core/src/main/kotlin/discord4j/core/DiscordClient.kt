package discord4j.core

import discord4j.core.`object`.Invite
import discord4j.core.`object`.Region
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.GuildCreateSpec
import discord4j.core.spec.UserEditSpec


/**
 * Requests to retrieve the channel represented by the supplied ID.
 *
 * @param id The ID of the channel.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getChannelById] where,
 * upon a non empty successful competition, returns the [Channel]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitChannel(id: Snowflake): Channel? = getChannelById(id).awaitNull()

/**
 * Requests to retrieve the guild represented by the supplied ID.
 *
 * @param id The ID of the guild.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getGuildById] where,
 * upon a non empty successful competition, returns the [Guild]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitGuild(id: Snowflake): Guild? = getGuildById(id).awaitNull()

/**
 * Requests to retrieve the emoji represented by the supplied IDs.
 *
 * @param emoji The ID of the emoji.
 * @param guild The ID of the guild.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getGuildEmojiById] where,
 * upon a non empty successful competition, returns the [GuildEmoji]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitGuildEmoji(emoji: Snowflake, guild: Snowflake): GuildEmoji? =
    getGuildEmojiById(guild, emoji).awaitNull()

/**
 * Requests to retrieve the member represented by the supplied IDs.
 *
 * @param member The ID of the member.
 * @param guild The ID of the guild.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getMemberById] where,
 * upon a non empty successful competition, returns the [Member]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitMember(member: Snowflake, guild: Snowflake): Member? =
    getMemberById(guild, member).awaitNull()

/**
 * Requests to retrieve the member represented by the supplied IDs.
 *
 * @param message The ID of the message.
 * @param channel The ID of the channel.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getMessageById] where,
 * upon a non empty successful competition, returns the [Message]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitMessage(message: Snowflake, channel: Snowflake): Message? =
    getMessageById(channel, message).awaitNull()

/**
 * Requests to retrieve the role represented by the supplied IDs.
 *
 * @param role The ID of the role.
 * @param guild The ID of the guild.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getRoleById] where,
 * upon a non empty successful competition, returns the [Role]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitRole(role: Snowflake, guild: Snowflake): Role? = getRoleById(guild, role).awaitNull()

/**
 * Requests to retrieve the user represented by the supplied ID.
 *
 * @param user The ID of the user.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getUserById] where,
 * upon a non empty successful competition, returns the [User]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitUser(user: Snowflake): User? = getUserById(user).awaitNull()

/**
 * Requests to retrieve the webhook represented by the supplied ID.
 *
 * @param webhook The ID of the webhook.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getWebhookById] where,
 * upon a non empty successful competition, returns the [Webhook]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitWebhook(webhook: Snowflake): Webhook? = getWebhookById(webhook).awaitNull()

/**
 * Requests to retrieve the application info of the current bot.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getApplicationInfo] where,
 * upon a non empty successful competition, returns the [ApplicationInfo]. If an error is received, it is thrown.
 */
suspend fun DiscordClient.applicationInfo(): ApplicationInfo = applicationInfo.await()

/**
 * Requests to retrieve the guilds the current client is in.
 *
 * @return A suspended call to the [reactor.core.publisher.Flux] from [DiscordClient.getGuilds] where,
 * upon a non empty successful competition, returns a [List] of [Guild]s. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitGuilds(): List<Guild> = guilds.await()

/**
 * Requests to retrieve the users the current client can see.
 *
 * @return A suspended call to the [reactor.core.publisher.Flux] from [DiscordClient.getUsers] where,
 * upon a non empty successful competition, returns a [List] of [User]s. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitUsers(): List<User> = users.await()

/**
 * Requests to retrieve the regions that are available.
 *
 * @return A suspended call to the [reactor.core.publisher.Flux] from [DiscordClient.getRegions] where,
 * upon a non empty successful competition, returns a [List] of [Region]s. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitRegions(): List<Region> = regions.await()

/**
 * Requests to retrieve the current user.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getSelf] where,
 * upon a non empty successful competition, returns the current [User]. If an error is received, it is thrown.
 */
suspend fun DiscordClient.awaitSelf(): User = self.await()

/**
 * Requests the current user's ID.
 *
 * @return A Snowflake? represented by the value of the [java.util.Optional] if it's present, or null if it's not.
 */
fun DiscordClient.nullableSelfId(): Snowflake? = selfId.grab()

/**
 * Logs in the client to the gateway.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.login] that completes
 * when the client disconnects from the gateway without a reconnect attempt. It is recommended to call this
 * from the main method as a final statement.
 */
suspend fun DiscordClient.awaitLogin(): Unit = login().unit()

/**
 * Logs out the client from the gateway.
 *
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.logout] that completes
 * when the client is fully disconnected from the gateway.
 */
suspend fun DiscordClient.awaitLogout(): Unit = logout().unit()


/**
 * Requests to create a guild.
 *
 * @param spec A consumer that provides a "blank" [GuildCreateSpec] to be operated on.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.createGuild] that, upon
 * successful completion, returns the created [Guild]. If an error is received, it's thrown.
 */
suspend fun DiscordClient.newGuild(spec: (GuildCreateSpec) -> Unit): Guild = createGuild(spec).await()

/**
 * Update this client [Presence].
 *
 * @param presence The updated client presence.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.updatePresence] that, upon
 * successful completion, does nothing. If an error is received, it's thrown.
 */
suspend fun DiscordClient.changePresence(presence: Presence): Unit = updatePresence(presence).unit()

/**
 * Requests to retrieve the invite represented by the supplied code.
 *
 * @param code The code of the invite.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.getInvite] where,
 * upon a non empty successful competition, returns the [Invite]. If the [reactor.core.publisher.Mono] is empty,
 * it returns null. If an error is received, it is thrown.
 */
suspend fun DiscordClient.nullableInvite(code: String): Invite? = getInvite(code).awaitNull()

/**
 * Requests to update the current user.
 *
 * @param spec A consumer that provides a "blank" [UserEditSpec] to be operated on.
 * @return A suspended call to the [reactor.core.publisher.Mono] from [DiscordClient.edit] that, upon
 * successful completion, returns the edited [User]. If an error is received, it's thrown.
 */
suspend fun DiscordClient.update(spec: (UserEditSpec) -> Unit): User = edit(spec).await()
