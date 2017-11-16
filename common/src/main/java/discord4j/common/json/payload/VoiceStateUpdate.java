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
package discord4j.common.json.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

public class VoiceStateUpdate implements Payload {

	@JsonProperty("guild_id")
	@UnsignedJson
	private final long guildId;
	@JsonProperty("channel_id")
	@UnsignedJson
	private final long channelId; // TODO nullable?
	@JsonProperty("self_mute")
	private final boolean selfMute;
	@JsonProperty("self_deaf")
	private final boolean selfDeaf;

	public VoiceStateUpdate(long guildId, long channelId, boolean selfMute, boolean selfDeaf) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.selfMute = selfMute;
		this.selfDeaf = selfDeaf;
	}
}
