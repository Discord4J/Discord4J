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
package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.OptionalField;

import java.util.Optional;

/**
 * Represents a Guild Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#guild-object">Guild Object</a>
 */
public class GuildPojo {

	private String id;
	private String name;
	private String icon;
	private String splash;
	private String owner_id;
	private String region;
	@JsonProperty("afk_channel_id")
	private String afkChannelId;
	@JsonProperty("verification_level")
	private int verificationLevel;
	@JsonProperty("default_message_notifications")
	private int defaultMessageNotifications;
	@JsonProperty("explicit_content_filter")
	private int explicitContentFilter;
	private RolePojo[] roles;
	private EmojiPojo[] emojis;
	private String[] features;
	private int mfa_level;
	@JsonProperty("application_id")
	private Optional<String> applicationId;
	@JsonProperty("widget_enabled")
	private boolean widgetEnabled;
	@JsonProperty("widget_channel_id")
	private String widgetChannelId;

	// GUILD_CREATE only
	@JsonProperty("joined_at")
	private OptionalField<String> joinedAt;
	private OptionalField<Boolean> large;
	private OptionalField<Boolean> unavailable;
	@JsonProperty("member_count")
	private OptionalField<Integer> memberCount;
	@JsonProperty("voice_states")
	private OptionalField<VoiceStatePojo[]> voiceStates;
	private OptionalField<GuildMemberPojo[]> members;
	private OptionalField<ChannelPojo[]> channels;
	private OptionalField<PresencePojo[]> presences;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getSplash() {
		return splash;
	}

	public void setSplash(String splash) {
		this.splash = splash;
	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAfkChannelId() {
		return afkChannelId;
	}

	public void setAfkChannelId(String afkChannelId) {
		this.afkChannelId = afkChannelId;
	}

	public int getVerificationLevel() {
		return verificationLevel;
	}

	public void setVerificationLevel(int verificationLevel) {
		this.verificationLevel = verificationLevel;
	}

	public int getDefaultMessageNotifications() {
		return defaultMessageNotifications;
	}

	public void setDefaultMessageNotifications(int defaultMessageNotifications) {
		this.defaultMessageNotifications = defaultMessageNotifications;
	}

	public int getExplicitContentFilter() {
		return explicitContentFilter;
	}

	public void setExplicitContentFilter(int explicitContentFilter) {
		this.explicitContentFilter = explicitContentFilter;
	}

	public RolePojo[] getRoles() {
		return roles;
	}

	public void setRoles(RolePojo[] roles) {
		this.roles = roles;
	}

	public EmojiPojo[] getEmojis() {
		return emojis;
	}

	public void setEmojis(EmojiPojo[] emojis) {
		this.emojis = emojis;
	}

	public String[] getFeatures() {
		return features;
	}

	public void setFeatures(String[] features) {
		this.features = features;
	}

	public int getMfa_level() {
		return mfa_level;
	}

	public void setMfa_level(int mfa_level) {
		this.mfa_level = mfa_level;
	}

	public Optional<String> getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Optional<String> applicationId) {
		this.applicationId = applicationId;
	}

	public boolean isWidgetEnabled() {
		return widgetEnabled;
	}

	public void setWidgetEnabled(boolean widgetEnabled) {
		this.widgetEnabled = widgetEnabled;
	}

	public String getWidgetChannelId() {
		return widgetChannelId;
	}

	public void setWidgetChannelId(String widgetChannelId) {
		this.widgetChannelId = widgetChannelId;
	}

	public OptionalField<String> getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(OptionalField<String> joinedAt) {
		this.joinedAt = joinedAt;
	}

	public OptionalField<Boolean> getLarge() {
		return large;
	}

	public void setLarge(OptionalField<Boolean> large) {
		this.large = large;
	}

	public OptionalField<Boolean> getUnavailable() {
		return unavailable;
	}

	public void setUnavailable(OptionalField<Boolean> unavailable) {
		this.unavailable = unavailable;
	}

	public OptionalField<Integer> getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(OptionalField<Integer> memberCount) {
		this.memberCount = memberCount;
	}

	public OptionalField<VoiceStatePojo[]> getVoiceStates() {
		return voiceStates;
	}

	public void setVoiceStates(OptionalField<VoiceStatePojo[]> voiceStates) {
		this.voiceStates = voiceStates;
	}

	public OptionalField<GuildMemberPojo[]> getMembers() {
		return members;
	}

	public void setMembers(OptionalField<GuildMemberPojo[]> members) {
		this.members = members;
	}

	public OptionalField<ChannelPojo[]> getChannels() {
		return channels;
	}

	public void setChannels(OptionalField<ChannelPojo[]> channels) {
		this.channels = channels;
	}

	public OptionalField<PresencePojo[]> getPresences() {
		return presences;
	}

	public void setPresences(OptionalField<PresencePojo[]> presences) {
		this.presences = presences;
	}
}
