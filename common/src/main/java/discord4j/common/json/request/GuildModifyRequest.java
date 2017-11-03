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
package discord4j.common.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.PossibleLong;
import discord4j.common.jackson.UnsignedJson;

@PossibleJson
public class GuildModifyRequest {

	private final Possible<String> name;
	private final Possible<String> region;
	@JsonProperty("verification_level")
	private final Possible<Integer> verificationLevel;
	@JsonProperty("default_message_notifications")
	private final Possible<Integer> defaultMessageNoficiations;
	@JsonProperty("afk_channel_id")
	@UnsignedJson
	private final PossibleLong afkChannelId;
	@JsonProperty("afk_timeout")
	private final Possible<Integer> afkTimeout;
	private final Possible<String> icon;
	@JsonProperty("owner_id")
	@UnsignedJson
	private final PossibleLong ownerId;
	private final Possible<String> splash;

	public GuildModifyRequest(Possible<String> name, Possible<String> region,
	                          Possible<Integer> verificationLevel,
	                          Possible<Integer> defaultMessageNoficiations,
	                          PossibleLong afkChannelId,
	                          Possible<Integer> afkTimeout, Possible<String> icon,
	                          PossibleLong ownerId, Possible<String> splash) {
		this.name = name;
		this.region = region;
		this.verificationLevel = verificationLevel;
		this.defaultMessageNoficiations = defaultMessageNoficiations;
		this.afkChannelId = afkChannelId;
		this.afkTimeout = afkTimeout;
		this.icon = icon;
		this.ownerId = ownerId;
		this.splash = splash;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Possible<String> name = Possible.absent();
		private Possible<String> region = Possible.absent();
		private Possible<Integer> verificationLevel = Possible.absent();
		private Possible<Integer> defaultMessageNoficiations = Possible.absent();
		private PossibleLong afkChannelId = PossibleLong.absent();
		private Possible<Integer> afkTimeout = Possible.absent();
		private Possible<String> icon = Possible.absent();
		private PossibleLong ownerId = PossibleLong.absent();
		private Possible<String> splash = Possible.absent();

		public Builder name(String name) {
			this.name = Possible.of(name);
			return this;
		}

		public Builder region(String region) {
			this.region = Possible.of(region);
			return this;
		}

		public Builder verificationLevel(int verificationLevel) {
			this.verificationLevel = Possible.of(verificationLevel);
			return this;
		}

		public Builder defaultMessageNoficiations(int defaultMessageNoficiations) {
			this.defaultMessageNoficiations = Possible.of(defaultMessageNoficiations);
			return this;
		}

		public Builder afkChannelId(long afkChannelId) {
			this.afkChannelId = PossibleLong.of(afkChannelId);
			return this;
		}

		public Builder afkTimeout(int afkTimeout) {
			this.afkTimeout = Possible.of(afkTimeout);
			return this;
		}

		public Builder icon(String icon) {
			this.icon = Possible.of(icon);
			return this;
		}

		public Builder ownerId(long ownerId) {
			this.ownerId = PossibleLong.of(ownerId);
			return this;
		}

		public Builder splash(String splash) {
			this.splash = Possible.of(splash);
			return this;
		}

		public GuildModifyRequest build() {
			return new GuildModifyRequest(name, region, verificationLevel, defaultMessageNoficiations, afkChannelId, afkTimeout, icon, ownerId, splash);
		}
	}
}
