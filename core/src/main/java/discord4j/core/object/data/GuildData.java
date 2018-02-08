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
package discord4j.core.object.data;

import javax.annotation.Nullable;

public class GuildData {

	private final long id;
	private final String name;
	@Nullable
	private final String icon;
	@Nullable
	private final String splash;
	private final long ownerId;
	private final String region;
	@Nullable
	private final Long afkChannelId;
	private final int afkTimeout;
	private final boolean embedEnabled;
	@Nullable
	private final Long embedChannelId;
	private final int verificationLevel;
	private final int defaultMessageNotifications;
	private final int explicitContentFilter;
	private final long[] roles;
	private final long[] emojis;
	private final String[] features;
	private final int mfaLevel;
	@Nullable
	private final Long applicationId;
	private final boolean widgetEnabled;
	@Nullable
	private final Long widgetChannelId;
	@Nullable
	private final String joinedAt;
	@Nullable
	private final Boolean large;
	@Nullable
	private final Integer memberCount;
	private final long[] members;
	private final long[] channels;
	@Nullable
	private final Long systemChannelId;

	public GuildData(long id, String name, @Nullable String icon, @Nullable String splash, long ownerId, String region,
			@Nullable Long afkChannelId, int afkTimeout, boolean embedEnabled, @Nullable Long embedChannelId,
			int verificationLevel, int defaultMessageNotifications, int explicitContentFilter, long[] roles,
			long[] emojis, String[] features, int mfaLevel, @Nullable Long applicationId, boolean widgetEnabled,
			@Nullable Long widgetChannelId, @Nullable String joinedAt, @Nullable Boolean large, @Nullable Integer
			memberCount, long[] members, long[] channels, @Nullable Long systemChannelId) {
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.splash = splash;
		this.ownerId = ownerId;
		this.region = region;
		this.afkChannelId = afkChannelId;
		this.afkTimeout = afkTimeout;
		this.embedEnabled = embedEnabled;
		this.embedChannelId = embedChannelId;
		this.verificationLevel = verificationLevel;
		this.defaultMessageNotifications = defaultMessageNotifications;
		this.explicitContentFilter = explicitContentFilter;
		this.roles = roles;
		this.emojis = emojis;
		this.features = features;
		this.mfaLevel = mfaLevel;
		this.applicationId = applicationId;
		this.widgetEnabled = widgetEnabled;
		this.widgetChannelId = widgetChannelId;
		this.joinedAt = joinedAt;
		this.large = large;
		this.memberCount = memberCount;
		this.members = members;
		this.channels = channels;
		this.systemChannelId = systemChannelId;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getIcon() {
		return icon;
	}

	@Nullable
	public String getSplash() {
		return splash;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public String getRegion() {
		return region;
	}

	@Nullable
	public Long getAfkChannelId() {
		return afkChannelId;
	}

	public int getAfkTimeout() {
		return afkTimeout;
	}

	public boolean isEmbedEnabled() {
		return embedEnabled;
	}

	@Nullable
	public Long getEmbedChannelId() {
		return embedChannelId;
	}

	public int getVerificationLevel() {
		return verificationLevel;
	}

	public int getDefaultMessageNotifications() {
		return defaultMessageNotifications;
	}

	public int getExplicitContentFilter() {
		return explicitContentFilter;
	}

	public long[] getRoles() {
		return roles;
	}

	public long[] getEmojis() {
		return emojis;
	}

	public String[] getFeatures() {
		return features;
	}

	public int getMfaLevel() {
		return mfaLevel;
	}

	@Nullable
	public Long getApplicationId() {
		return applicationId;
	}

	public boolean isWidgetEnabled() {
		return widgetEnabled;
	}

	@Nullable
	public Long getWidgetChannelId() {
		return widgetChannelId;
	}

	@Nullable
	public String getJoinedAt() {
		return joinedAt;
	}

	@Nullable
	public Boolean getLarge() {
		return large;
	}

	@Nullable
	public Integer getMemberCount() {
		return memberCount;
	}

	public long[] getMembers() {
		return members;
	}

	public long[] getChannels() {
		return channels;
	}

	@Nullable
	public Long getSystemChannelId() {
		return systemChannelId;
	}
}
