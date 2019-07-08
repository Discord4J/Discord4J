package discord4j.core.`object`.entity

import discord4j.core.`object`.Ban
import discord4j.core.`object`.ExtendedInvite
import discord4j.core.`object`.Region
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.audit.AuditLogEntry
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.util.Image
import discord4j.core.`object`.util.Snowflake
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.grab
import discord4j.core.spec.*
import discord4j.core.unit
import java.time.Instant

// TODO: DOCS

fun Guild.nullableIconUrl(format: Image.Format): String? = getIconUrl(format).grab()
fun Guild.nullableSplashUrl(format: Image.Format): String? = getSplashUrl(format).grab()
suspend fun Guild.awaitOwner(): User = owner.await()
suspend fun Guild.awaitRegion(): Region = region.await()
suspend fun Guild.awaitRegions(): List<Region> = regions.await()
fun Guild.nullableAfkChannelId(): Snowflake? = afkChannelId.grab()
suspend fun Guild.awaitAfkChannel(): VoiceChannel? = afkChannel.awaitNull()
fun Guild.nullableEmbedChannelId(): Snowflake? = embedChannelId.grab()
suspend fun Guild.awaitEmbedChannel(): GuildChannel? = embedChannel.awaitNull()
suspend fun Guild.awaitRoles(): List<Role> = roles.await()
suspend fun Guild.awaitRole(id: Snowflake): Role = getRoleById(id).await()
suspend fun Guild.awaitEveryoneRole(): Role = awaitRole(id)
suspend fun Guild.awaitEmojis(): List<GuildEmoji> = emojis.await()
suspend fun Guild.awaitEmoji(id: Snowflake): GuildEmoji = getGuildEmojiById(id).await()
fun Guild.nullableApplicationId(): Snowflake? = applicationId.grab()
fun Guild.nullableWidgetChannelId(): Snowflake? = widgetChannelId.grab()
suspend fun Guild.awaitWidgetChannel(): GuildChannel = widgetChannel.await()
fun Guild.nullableSystemChannelId(): Snowflake? = systemChannelId.grab()
suspend fun Guild.awaitSystemChannel(): TextChannel? = systemChannel.await()
fun Guild.nullableJoinTime(): Instant? = joinTime.grab()
fun Guild.nullableLarge(): Boolean? = isLarge.grab()
fun Guild.nullableMemberCount(): Int? = memberCount.grab()
suspend fun Guild.awaitVoiceStates(): List<VoiceState> = voiceStates.await()
suspend fun Guild.awaitMembers(): List<Member> = members.await()
suspend fun Guild.awaitMember(id: Snowflake): Member = getMemberById(id).await()
suspend fun Guild.awaitChannels(): List<GuildChannel> = channels.await()
suspend fun Guild.awaitChannel(id: Snowflake): GuildChannel = getChannelById(id).await()
suspend fun Guild.awaitPresences(): List<Presence> = presences.await()
suspend fun Guild.update(spec: (GuildEditSpec) -> Unit): Guild = edit(spec).await()
suspend fun Guild.newRole(spec: (RoleCreateSpec) -> Unit): Role = createRole(spec).await()
suspend fun Guild.newCategory(spec: (CategoryCreateSpec) -> Unit): Category = createCategory(spec).await()
suspend fun Guild.newTextChannel(spec: (TextChannelCreateSpec) -> Unit): TextChannel = createTextChannel(spec).await()
suspend fun Guild.newVoiceChannel(spec: (VoiceChannelCreateSpec) -> Unit): VoiceChannel =
    createVoiceChannel(spec).await()

suspend fun Guild.awaitDelete(): Unit = delete().unit()
suspend fun Guild.awaitKick(id: Snowflake, reason: String? = null): Unit = kick(id, reason).unit()
suspend fun Guild.awaitBans(): List<Ban> = bans.await()
suspend fun Guild.awaitBan(id: Snowflake): Ban = getBan(id).await()
suspend fun Guild.awaitBan(id: Snowflake, spec: (BanQuerySpec) -> Unit): Unit = ban(id, spec).unit()
suspend fun Guild.awaitUnban(id: Snowflake, reason: String? = null): Unit = unban(id, reason).unit()
suspend fun Guild.awaitPruneCount(days: Int): Int = getPruneCount(days).await()
suspend fun Guild.awaitPrune(days: Int, reason: String? = null): Int = prune(days, reason).await()
suspend fun Guild.awaitLeave(): Unit = leave().unit()
suspend fun Guild.awaitAuditLog(spec: (AuditLogQuerySpec) -> Unit = {}): List<AuditLogEntry> = getAuditLog(spec).await()
suspend fun Guild.awaitWebhooks(): List<Webhook> = webhooks.await()
suspend fun Guild.awaitInvites(): List<ExtendedInvite> = invites.await()
suspend fun Guild.editSelfNickname(nickname: String): String = changeSelfNickname(nickname).await()
