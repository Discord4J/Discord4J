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
package discord4j.core.object.spec;

import discord4j.common.json.request.GuildModifyRequest;
import discord4j.core.object.Region;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;

import javax.annotation.Nullable;

public class GuildEditSpec implements Spec<GuildModifyRequest> {

	private final GuildModifyRequest.Builder requestBuilder = GuildModifyRequest.builder();

	public void setName(String name) {
		requestBuilder.name(name);
	}

	public void setRegion(Region region) {
		requestBuilder.region(region.getId());
	}

	public void setVerificationLevel(Guild.VerificationLevel verificationLevel) {
		requestBuilder.verificationLevel(verificationLevel.getValue());
	}

	public void setDefaultMessageNotificationsLevel(Guild.NotificationLevel notificationsLevel) {
		requestBuilder.defaultMessageNoficiations(notificationsLevel.getValue());
	}

	public void setAfkChannelId(@Nullable Snowflake afkChannelId) {
		requestBuilder.afkChannelId(afkChannelId == null ? null : afkChannelId.asLong());
	}

	public void setAfkChannel(@Nullable VoiceChannel afkChannel) {
		setAfkChannelId(afkChannel == null ? null : afkChannel.getId());
	}

	public void setAfkTimeout(int afkTimeout) {
		requestBuilder.afkTimeout(afkTimeout);
	}

	public void setIcon(@Nullable String icon) { // TODO Icon class
		requestBuilder.icon(icon);
	}

	public void setOwnerId(Snowflake ownerId) {
		requestBuilder.ownerId(ownerId.asLong());
	}

	public void setOwner(Member member) {
		setOwnerId(member.getId());
	}

	public void setSplash(@Nullable String splash) {
		requestBuilder.splash(splash);
	}

	@Override
	public GuildModifyRequest asRequest() {
		return requestBuilder.build();
	}
}
