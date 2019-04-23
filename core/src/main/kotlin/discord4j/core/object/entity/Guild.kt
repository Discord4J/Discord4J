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

fun Guild.awaitIconUrl(format: Image.Format): String? = getIconUrl(format).grab()
fun Guild.awaitSplashUrl(format: Image.Format): String? = getSplashUrl(format).grab()
suspend fun Guild.owner(): User = owner.await()
suspend fun Guild.region(): Region = region.await()
suspend fun Guild.regions(): List<Region> = regions.await()
fun Guild.afkChannelId(): Snowflake? = afkChannelId.grab()
suspend fun Guild.afkChannel(): VoiceChannel? = afkChannel.awaitNull()
fun Guild.embedChannelId(): Snowflake? = embedChannelId.grab()
suspend fun Guild.embedChannel(): GuildChannel? = embedChannel.awaitNull()
suspend fun Guild.roles(): List<Role> = roles.await()
suspend fun Guild.awaitRole(id: Snowflake): Role = getRoleById(id).await()
suspend fun Guild.awaitEveryoneRole(): Role = awaitRole(id)
suspend fun Guild.awaitEmojis(): List<GuildEmoji> = emojis.await()
suspend fun Guild.awaitEmoji(id: Snowflake): GuildEmoji = getGuildEmojiById(id).await()
fun Guild.applicationId(): Snowflake? = applicationId.grab()
fun Guild.widgetChannelId(): Snowflake? = widgetChannelId.grab()
suspend fun Guild.widgetChannel(): GuildChannel = widgetChannel.await()
fun Guild.systemChannelId(): Snowflake? = systemChannelId.grab()
suspend fun Guild.systemChannel(): TextChannel? = systemChannel.await()
fun Guild.joinTime(): Instant? = joinTime.grab()
fun Guild.large(): Boolean? = isLarge.grab()
fun Guild.memberCount(): Int? = memberCount.grab()
suspend fun Guild.voiceStates(): List<VoiceState> = voiceStates.await()
suspend fun Guild.members(): List<Member> = members.await()
suspend fun Guild.awaitMember(id: Snowflake): Member = getMemberById(id).await()
suspend fun Guild.channels(): List<GuildChannel> = channels.await()
suspend fun Guild.awaitChannel(id: Snowflake): GuildChannel = getChannelById(id).await()
suspend fun Guild.presences(): List<Presence> = presences.await()
suspend fun Guild.update(spec: (GuildEditSpec) -> Unit): Guild = edit(spec).await()
suspend fun Guild.newRole(spec: (RoleCreateSpec) -> Unit): Role = createRole(spec).await()
suspend fun Guild.newCategory(spec: (CategoryCreateSpec) -> Unit): Category = createCategory(spec).await()
suspend fun Guild.newTextChannel(spec: (TextChannelCreateSpec) -> Unit): TextChannel = createTextChannel(spec).await()
suspend fun Guild.newVoiceChannel(spec: (VoiceChannelCreateSpec) -> Unit): VoiceChannel =
    createVoiceChannel(spec).await()

suspend fun Guild.awaitDelete(): Unit = delete().unit()
suspend fun Guild.awaitKick(id: Snowflake, reason: String? = null): Unit = kick(id, reason).unit()
suspend fun Guild.bans(): List<Ban> = bans.await()
suspend fun Guild.awaitBan(id: Snowflake): Ban = getBan(id).await()
suspend fun Guild.awaitBan(id: Snowflake, spec: (BanQuerySpec) -> Unit): Unit = ban(id, spec).unit()
suspend fun Guild.awaitUnban(id: Snowflake, reason: String? = null): Unit = unban(id, reason).unit()
suspend fun Guild.awaitPruneCount(days: Int): Int = getPruneCount(days).await()
suspend fun Guild.awaitPrune(days: Int, reason: String? = null): Int = prune(days, reason).await()
suspend fun Guild.awaitLeave(): Unit = leave().unit()
suspend fun Guild.awaitAuditLog(spec: (AuditLogQuerySpec) -> Unit = {}): List<AuditLogEntry> = getAuditLog(spec).await()
suspend fun Guild.webhooks(): List<Webhook> = webhooks.await()
suspend fun Guild.invites(): List<ExtendedInvite> = invites.await()
suspend fun Guild.editSelfNickname(nickname: String): String = changeSelfNickname(nickname).await()
