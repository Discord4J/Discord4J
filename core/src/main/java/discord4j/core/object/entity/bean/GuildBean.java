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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity.bean;

import discord4j.common.json.response.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class GuildBean implements Serializable {

    private static final long serialVersionUID = -133099254066154899L;

    private long id;
    private String name;
    @Nullable
    private String icon;
    @Nullable
    private String splash;
    private long ownerId;
    private String region;
    @Nullable
    private Long afkChannelId;
    private int afkTimeout;
    private Long embedChannelId;
    private int verificationLevel;
    private int defaultMessageNotifications;
    private int explicitContentFilter;
    private long[] roles;
    private long[] emojis;
    private String[] features;
    private int mfaLevel;
    @Nullable
    private Long applicationId;
    @Nullable
    private Long widgetChannelId;
    @Nullable
    private Long systemChannelId;
    @Nullable
    private String joinedAt;
    @Nullable
    private Boolean large;
    @Nullable
    private Integer memberCount;
    // TODO VoiceStates
    @Nullable
    private long[] members;
    @Nullable
    private long[] channels;
    // TODO Presences

    public GuildBean(final GuildResponse response) {
        id = response.getId();
        name = response.getName();
        icon = response.getIcon();
        splash = response.getSplash();
        ownerId = response.getOwnerId();
        region = response.getRegion();
        afkChannelId = response.getAfkChannelId();
        afkTimeout = response.getAfkTimeout();
        embedChannelId = response.getEmbedChannelId();
        verificationLevel = response.getVerificationLevel();
        defaultMessageNotifications = response.getDefaultMessageNotifications();
        explicitContentFilter = response.getExplciitContentFilter();

        roles = Arrays.stream(response.getRoles())
                .mapToLong(RoleResponse::getId)
                .toArray();

        emojis = Arrays.stream(response.getEmojis())
                .mapToLong(emoji -> Objects.requireNonNull(emoji.getId()))
                .toArray();

        features = response.getFeatures();
        mfaLevel = response.getMfaLevel();
        applicationId = response.getApplicationId();
        widgetChannelId = response.getWidgetChannelId();
        systemChannelId = response.getSystemChannelId();
        joinedAt = response.getJoinedAt();
        // TODO VoiceStates

        final GuildMemberResponse[] guildMembers = response.getMembers();
        members = (guildMembers == null) ? null : Arrays.stream(guildMembers)
                .map(GuildMemberResponse::getUser)
                .mapToLong(UserResponse::getId)
                .toArray();

        final ChannelResponse[] guildChannels = response.getChannels();
        channels = (guildChannels == null) ? null : Arrays.stream(guildChannels)
                .mapToLong(ChannelResponse::getId)
                .toArray();

        // TODO Presences
    }

    public GuildBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable final String icon) {
        this.icon = icon;
    }

    @Nullable
    public String getSplash() {
        return splash;
    }

    public void setSplash(@Nullable final String splash) {
        this.splash = splash;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public void setAfkChannelId(@Nullable final Long afkChannelId) {
        this.afkChannelId = afkChannelId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    public void setAfkTimeout(final int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public void setEmbedChannelId(final Long embedChannelId) {
        this.embedChannelId = embedChannelId;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public void setVerificationLevel(final int verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public void setDefaultMessageNotifications(final int defaultMessageNotifications) {
        this.defaultMessageNotifications = defaultMessageNotifications;
    }

    public int getExplicitContentFilter() {
        return explicitContentFilter;
    }

    public void setExplicitContentFilter(final int explicitContentFilter) {
        this.explicitContentFilter = explicitContentFilter;
    }

    public long[] getRoles() {
        return roles;
    }

    public void setRoles(final long[] roles) {
        this.roles = roles;
    }

    public long[] getEmojis() {
        return emojis;
    }

    public void setEmojis(final long[] emojis) {
        this.emojis = emojis;
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(final String[] features) {
        this.features = features;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    public void setMfaLevel(final int mfaLevel) {
        this.mfaLevel = mfaLevel;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(@Nullable final Long applicationId) {
        this.applicationId = applicationId;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
    }

    public void setWidgetChannelId(@Nullable final Long widgetChannelId) {
        this.widgetChannelId = widgetChannelId;
    }

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    public void setSystemChannelId(@Nullable final Long systemChannelId) {
        this.systemChannelId = systemChannelId;
    }

    @Nullable
    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(@Nullable final String joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Nullable
    public Boolean getLarge() {
        return large;
    }

    public void setLarge(@Nullable final Boolean large) {
        this.large = large;
    }

    @Nullable
    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(@Nullable final Integer memberCount) {
        this.memberCount = memberCount;
    }

    @Nullable
    public long[] getMembers() {
        return members;
    }

    public void setMembers(@Nullable final long[] members) {
        this.members = members;
    }

    @Nullable
    public long[] getChannels() {
        return channels;
    }

    public void setChannels(@Nullable final long[] channels) {
        this.channels = channels;
    }
}
