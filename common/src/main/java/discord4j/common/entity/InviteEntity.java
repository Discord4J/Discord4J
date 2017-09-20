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
package discord4j.common.entity;

import discord4j.common.jackson.DiscordEntity;

/**
 * Represents an Invite Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/invite#invite-object">Invite Object</a>
 */
@DiscordEntity
public class InviteEntity {

	private String code;
	private GuildEntity guild;
	private ChannelEntity channel;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public GuildEntity getGuild() {
		return guild;
	}

	public void setGuild(GuildEntity guild) {
		this.guild = guild;
	}

	public ChannelEntity getChannel() {
		return channel;
	}

	public void setChannel(ChannelEntity channel) {
		this.channel = channel;
	}
}
