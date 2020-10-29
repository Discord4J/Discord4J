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

/**
 * GuildData with mutable role/emoji/member/channel ID set
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class StoredGuildData implements GuildData {

    private final Set<Long> roles;
    private final Set<Long> emojis;
    private final Set<Long> members;
    private final Set<Long> channels;
    private final String joinedAt;
    private final boolean large;
    private final Possible<Boolean> unavailable;
    private final int memberCount;
    private final String ownerId;
    private final String region;
    private final int afkTimeout;
    private final int defaultMessageNotifications;
    private final int explicitContentFilter;
    private final int mfaLevel;
    private final int premiumTier;
    private final Optional<String> preferredLocale;
    private final String id;
    private final String name;
    private final Optional<String> icon;
    private final Optional<String> splash;
    private final Optional<String> discoverySplash;
    private final Possible<Boolean> owner;
    private final Possible<Long> permissions;
    private final Optional<String> afkChannelId;
    private final Possible<Boolean> embedEnabled;
    private final Possible<Optional<String>> embedChannelId;
    private final List<String> features;
    private final Optional<String> applicationId;
    private final Possible<Boolean> widgetEnabled;
    private final Possible<Optional<String>> widgetChannelId;
    private final Optional<String> systemChannelId;
    private final OptionalInt systemChannelFlags;
    private final Optional<String> rulesChannelId;
    private final Possible<Optional<Integer>> maxPresences;
    private final Possible<Integer> maxMembers;
    private final Optional<String> vanityUrlCode;
    private final Optional<String> description;
    private final Optional<String> banner;
    private final Possible<Optional<Integer>> premiumSubscriptionCount;
    private final Optional<String> publicUpdatesChannelId;
    private final Possible<Integer> maxVideoChannelUsers;
    private final Possible<Integer> approximateMemberCount;
    private final Possible<Integer> approximatePresenceCount;
    private final int verificationLevel;
    
    StoredGuildData(GuildData original) {
        this.roles = ImplUtils.toLongIdSet(original.roles());
        this.emojis = ImplUtils.toLongIdSet(original.emojis());
        this.members = ImplUtils.toLongIdSet(original.members());
        this.channels = ImplUtils.toLongIdSet(original.channels());
        this.joinedAt = original.joinedAt();
        this.large = original.large();
        this.unavailable = original.unavailable();
        this.memberCount = original.memberCount();
        this.ownerId = original.ownerId();
        this.region = original.region();
        this.afkTimeout = original.afkTimeout();
        this.defaultMessageNotifications = original.defaultMessageNotifications();
        this.explicitContentFilter = original.explicitContentFilter();
        this.mfaLevel = original.mfaLevel();
        this.premiumTier = original.premiumTier();
        this.preferredLocale = original.preferredLocale();
        this.id = original.id();
        this.name = original.name();
        this.icon = original.icon();
        this.splash = original.splash();
        this.discoverySplash = original.discoverySplash();
        this.owner = original.owner();
        this.permissions = original.permissions();
        this.afkChannelId = original.afkChannelId();
        this.embedEnabled = original.embedEnabled();
        this.embedChannelId = original.embedChannelId();
        this.features = original.features();
        this.applicationId = original.applicationId();
        this.widgetEnabled = original.widgetEnabled();
        this.widgetChannelId = original.widgetChannelId();
        this.systemChannelId = original.systemChannelId();
        this.systemChannelFlags = original.systemChannelFlags();
        this.rulesChannelId = original.rulesChannelId();
        this.maxPresences = original.maxPresences();
        this.maxMembers = original.maxMembers();
        this.vanityUrlCode = original.vanityUrlCode();
        this.description = original.description();
        this.banner = original.banner();
        this.premiumSubscriptionCount = original.premiumSubscriptionCount();
        this.publicUpdatesChannelId = original.publicUpdatesChannelId();
        this.maxVideoChannelUsers = original.maxVideoChannelUsers();
        this.approximateMemberCount = original.approximateMemberCount();
        this.approximatePresenceCount = original.approximatePresenceCount();
        this.verificationLevel = original.verificationLevel();
    }

    @Override
    public List<String> roles() {
        return ImplUtils.toStringIdList(roles);
    }

    @Override
    public List<String> emojis() {
        return ImplUtils.toStringIdList(emojis);
    }

    @Override
    public List<String> members() {
        return ImplUtils.toStringIdList(members);
    }

    @Override
    public List<String> channels() {
        return ImplUtils.toStringIdList(channels);
    }

    public Set<Long> roleIdSet() {
        return roles;
    }

    public Set<Long> emojiIdSet() {
        return emojis;
    }

    public Set<Long> memberIdSet() {
        return members;
    }

    public Set<Long> channelIdSet() {
        return channels;
    }

    @Override
    public String joinedAt() {
        return joinedAt;
    }

    @Override
    public boolean large() {
        return large;
    }

    @Override
    public Possible<Boolean> unavailable() {
        return unavailable;
    }

    @Override
    public int memberCount() {
        return memberCount;
    }

    @Override
    public String ownerId() {
        return ownerId;
    }

    @Override
    public String region() {
        return region;
    }

    @Override
    public int afkTimeout() {
        return afkTimeout;
    }

    @Override
    public int defaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    @Override
    public int explicitContentFilter() {
        return explicitContentFilter;
    }

    @Override
    public int mfaLevel() {
        return mfaLevel;
    }

    @Override
    public int premiumTier() {
        return premiumTier;
    }

    @Override
    public Optional<String> preferredLocale() {
        return preferredLocale;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<String> icon() {
        return icon;
    }

    @Override
    public Optional<String> splash() {
        return splash;
    }

    @Override
    public Optional<String> discoverySplash() {
        return discoverySplash;
    }

    @Override
    public Possible<Boolean> owner() {
        return owner;
    }

    @Override
    public Possible<Long> permissions() {
        return permissions;
    }

    @Override
    public Optional<String> afkChannelId() {
        return afkChannelId;
    }

    @Override
    public Possible<Boolean> embedEnabled() {
        return embedEnabled;
    }

    @Override
    public Possible<Optional<String>> embedChannelId() {
        return embedChannelId;
    }

    @Override
    public List<String> features() {
        return features;
    }

    @Override
    public Optional<String> applicationId() {
        return applicationId;
    }

    @Override
    public Possible<Boolean> widgetEnabled() {
        return widgetEnabled;
    }

    @Override
    public Possible<Optional<String>> widgetChannelId() {
        return widgetChannelId;
    }

    @Override
    public Optional<String> systemChannelId() {
        return systemChannelId;
    }

    @Override
    public OptionalInt systemChannelFlags() {
        return systemChannelFlags;
    }

    @Override
    public Optional<String> rulesChannelId() {
        return rulesChannelId;
    }

    @Override
    public Possible<Optional<Integer>> maxPresences() {
        return maxPresences;
    }

    @Override
    public Possible<Integer> maxMembers() {
        return maxMembers;
    }

    @Override
    public Optional<String> vanityUrlCode() {
        return vanityUrlCode;
    }

    @Override
    public Optional<String> description() {
        return description;
    }

    @Override
    public Optional<String> banner() {
        return banner;
    }

    @Override
    public Possible<Optional<Integer>> premiumSubscriptionCount() {
        return premiumSubscriptionCount;
    }

    @Override
    public Optional<String> publicUpdatesChannelId() {
        return publicUpdatesChannelId;
    }

    @Override
    public Possible<Integer> maxVideoChannelUsers() {
        return maxVideoChannelUsers;
    }

    @Override
    public Possible<Integer> approximateMemberCount() {
        return approximateMemberCount;
    }

    @Override
    public Possible<Integer> approximatePresenceCount() {
        return approximatePresenceCount;
    }

    @Override
    public int verificationLevel() {
        return verificationLevel;
    }
}
