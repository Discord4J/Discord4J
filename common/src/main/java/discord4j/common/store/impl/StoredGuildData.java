/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common.store.impl;

import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.possible.Possible;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static discord4j.common.store.impl.ImplUtils.*;

/**
 * Guild data with snowflakes stored as long, and with mutable role/emoji/member/channel ID set.
 */
@SuppressWarnings("deprecation")
class StoredGuildData {

    private final Set<Long> roles;
    private final Set<Long> emojis;
    private final Set<Long> members;
    private final Set<Long> channels;
    private final String joinedAt;
    private final boolean large;
    private final boolean unavailable_value;
    private final boolean unavailable_absent;
    private final int memberCount;
    private final long ownerId;
    private final String region;
    private final int afkTimeout;
    private final int defaultMessageNotifications;
    private final int explicitContentFilter;
    private final int mfaLevel;
    private final int premiumTier;
    private final String preferredLocale;
    private final long id;
    private final String name;
    private final String icon;
    private final String splash;
    private final String discoverySplash;
    private final boolean owner_value;
    private final boolean owner_absent;
    private final long permissions_value;
    private final boolean permissions_absent;
    private final String afkChannelId;
    private final boolean embedEnabled_value;
    private final boolean embedEnabled_absent;
    private final String embedChannelId_value;
    private final boolean embedChannelId_absent;
    private final List<String> features;
    private final long applicationId;
    private final boolean applicationId_null;
    private final boolean widgetEnabled_value;
    private final boolean widgetEnabled_absent;
    private final long widgetChannelId_value;
    private final boolean widgetChannelId_absent;
    private final long systemChannelId;
    private final boolean systemChannelId_null;
    private final int systemChannelFlags;
    private final boolean systemChannelFlags_null;
    private final long rulesChannelId;
    private final boolean rulesChannelId_null;
    private final int maxPresences_value;
    private final boolean maxPresences_value_null;
    private final boolean maxPresences_absent;
    private final int maxMembers_value;
    private final boolean maxMembers_absent;
    private final String vanityUrlCode;
    private final String description;
    private final String banner;
    private final int premiumSubscriptionCount_value;
    private final boolean premiumSubscriptionCount_value_null;
    private final boolean premiumSubscriptionCount_absent;
    private final long publicUpdatesChannelId;
    private final boolean publicUpdatesChannelId_null;
    private final int maxVideoChannelUsers_value;
    private final boolean maxVideoChannelUsers_absent;
    private final int approximateMemberCount_value;
    private final boolean approximateMemberCount_absent;
    private final int approximatePresenceCount_value;
    private final boolean approximatePresenceCount_absent;
    private final int verificationLevel;
    
    StoredGuildData(GuildData original) {
        this.roles = toLongIdSet(original.roles());
        this.emojis = toLongIdSet(original.emojis());
        this.members = toLongIdSet(original.members());
        this.channels = toLongIdSet(original.channels());
        this.joinedAt = original.joinedAt();
        this.large = original.large();
        this.unavailable_value = original.unavailable().toOptional().orElse(false);
        this.unavailable_absent = original.unavailable().isAbsent();
        this.memberCount = original.memberCount();
        this.ownerId = toLongId(original.ownerId());
        this.region = original.region();
        this.afkTimeout = original.afkTimeout();
        this.defaultMessageNotifications = original.defaultMessageNotifications();
        this.explicitContentFilter = original.explicitContentFilter();
        this.mfaLevel = original.mfaLevel();
        this.premiumTier = original.premiumTier();
        this.preferredLocale = original.preferredLocale().orElse(null);
        this.id = toLongId(original.id());
        this.name = original.name();
        this.icon = original.icon().orElse(null);
        this.splash = original.splash().orElse(null);
        this.discoverySplash = original.discoverySplash().orElse(null);
        this.owner_value = original.owner().toOptional().orElse(false);
        this.owner_absent = original.owner().isAbsent();
        this.permissions_value = original.permissions().toOptional().orElse(-1L);
        this.permissions_absent = original.permissions().isAbsent();
        this.afkChannelId = original.afkChannelId().orElse(null);
        this.embedEnabled_value = original.embedEnabled().toOptional().orElse(false);
        this.embedEnabled_absent = original.embedEnabled().isAbsent();
        this.embedChannelId_value = Possible.flatOpt(original.embedChannelId()).orElse(null);
        this.embedChannelId_absent = original.embedChannelId().isAbsent();
        this.features = original.features();
        this.applicationId = original.applicationId().map(ImplUtils::toLongId).orElse(-1L);
        this.applicationId_null = !original.applicationId().map(ImplUtils::toLongId).isPresent();
        this.widgetEnabled_value = original.widgetEnabled().toOptional().orElse(false);
        this.widgetEnabled_absent = original.widgetEnabled().isAbsent();
        this.widgetChannelId_value = idFromPossibleOptionalString(original.widgetChannelId()).orElse(-1L);
        this.widgetChannelId_absent = original.widgetChannelId().isAbsent();
        this.systemChannelId = original.systemChannelId().map(ImplUtils::toLongId).orElse(-1L);
        this.systemChannelId_null = !original.systemChannelId().map(ImplUtils::toLongId).isPresent();
        this.systemChannelFlags = original.systemChannelFlags().isPresent() ?
                original.systemChannelFlags().getAsInt() : -1;
        this.systemChannelFlags_null = !original.systemChannelFlags().isPresent();
        this.rulesChannelId = original.rulesChannelId().map(ImplUtils::toLongId).orElse(-1L);
        this.rulesChannelId_null = !original.rulesChannelId().map(ImplUtils::toLongId).isPresent();
        this.maxPresences_value = Possible.flatOpt(original.maxPresences()).orElse(-1);
        this.maxPresences_value_null = !Possible.flatOpt(original.maxPresences()).isPresent();
        this.maxPresences_absent = original.maxPresences().isAbsent();
        this.maxMembers_value = original.maxMembers().toOptional().orElse(-1);
        this.maxMembers_absent = original.maxMembers().isAbsent();
        this.vanityUrlCode = original.vanityUrlCode().orElse(null);
        this.description = original.description().orElse(null);
        this.banner = original.banner().orElse(null);
        this.premiumSubscriptionCount_value = Possible.flatOpt(original.premiumSubscriptionCount()).orElse(-1);
        this.premiumSubscriptionCount_value_null = !Possible.flatOpt(original.premiumSubscriptionCount()).isPresent();
        this.premiumSubscriptionCount_absent = original.premiumSubscriptionCount().isAbsent();
        this.publicUpdatesChannelId = original.publicUpdatesChannelId().map(ImplUtils::toLongId).orElse(-1L);
        this.publicUpdatesChannelId_null = !original.publicUpdatesChannelId().map(ImplUtils::toLongId).isPresent();
        this.maxVideoChannelUsers_value = original.maxVideoChannelUsers().toOptional().orElse(-1);
        this.maxVideoChannelUsers_absent = original.maxVideoChannelUsers().isAbsent();
        this.approximateMemberCount_value = original.approximateMemberCount().toOptional().orElse(-1);
        this.approximateMemberCount_absent = original.approximateMemberCount().isAbsent();
        this.approximatePresenceCount_value = original.approximatePresenceCount().toOptional().orElse(-1);
        this.approximatePresenceCount_absent = original.approximatePresenceCount().isAbsent();
        this.verificationLevel = original.verificationLevel();
    }

    Set<Long> roleIdSet() {
        return roles;
    }

    Set<Long> emojiIdSet() {
        return emojis;
    }

    Set<Long> memberIdSet() {
        return members;
    }

    Set<Long> channelIdSet() {
        return channels;
    }

    GuildData toImmutable() {
        return GuildData.builder()
                .roles(toStringIdList(roles))
                .emojis(toStringIdList(emojis))
                .members(toStringIdList(members))
                .channels(toStringIdList(channels))
                .joinedAt(joinedAt)
                .large(large)
                .unavailable(toPossible(unavailable_value, unavailable_absent))
                .memberCount(memberCount)
                .ownerId("" + ownerId)
                .region(region)
                .afkTimeout(afkTimeout)
                .defaultMessageNotifications(defaultMessageNotifications)
                .explicitContentFilter(explicitContentFilter)
                .mfaLevel(mfaLevel)
                .premiumTier(premiumTier)
                .preferredLocale(Optional.ofNullable(preferredLocale))
                .id("" + id)
                .name(name)
                .icon(Optional.ofNullable(icon))
                .splash(Optional.ofNullable(splash))
                .discoverySplash(Optional.ofNullable(discoverySplash))
                .owner(toPossible(owner_value, owner_absent))
                .permissions(toPossible(permissions_value, permissions_absent))
                .afkChannelId(Optional.ofNullable(afkChannelId))
                .embedEnabled(toPossible(embedEnabled_value, embedEnabled_absent))
                .embedChannelId(toPossibleOptional(embedChannelId_value, embedChannelId_absent))
                .features(features)
                .applicationId(Optional.ofNullable(applicationId_null ? null : applicationId).map(String::valueOf))
                .widgetEnabled(toPossible(widgetEnabled_value, widgetEnabled_absent))
                .widgetChannelId(toPossibleOptionalStringId(widgetChannelId_value, widgetChannelId_absent))
                .systemChannelId(Optional.ofNullable(systemChannelId_null ? null : systemChannelId).map(String::valueOf))
                .systemChannelFlags(systemChannelFlags_null ? OptionalInt.empty() : OptionalInt.of(systemChannelFlags))
                .rulesChannelId(Optional.ofNullable(rulesChannelId_null ? null :rulesChannelId).map(String::valueOf))
                .maxPresences(toPossibleOptional(maxPresences_value, maxPresences_absent, maxPresences_value_null))
                .maxMembers(toPossible(maxMembers_value, maxMembers_absent))
                .vanityUrlCode(Optional.ofNullable(vanityUrlCode))
                .description(Optional.ofNullable(description))
                .banner(Optional.ofNullable(banner))
                .premiumSubscriptionCount(toPossibleOptional(premiumSubscriptionCount_value,
                        premiumSubscriptionCount_absent, premiumSubscriptionCount_value_null))
                .publicUpdatesChannelId(Optional.ofNullable(publicUpdatesChannelId_null ? null :
                        publicUpdatesChannelId).map(String::valueOf))
                .maxVideoChannelUsers(toPossible(maxVideoChannelUsers_value, maxVideoChannelUsers_absent))
                .approximateMemberCount(toPossible(approximateMemberCount_value, approximateMemberCount_absent))
                .approximatePresenceCount(toPossible(approximatePresenceCount_value, approximatePresenceCount_absent))
                .verificationLevel(verificationLevel)
                .build();
    }
}
