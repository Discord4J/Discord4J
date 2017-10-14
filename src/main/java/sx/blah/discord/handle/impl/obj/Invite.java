/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.json.objects.InviteObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IUser;

/**
 * The default implementation of {@link IInvite}.
 */
public class Invite implements IInvite {

	/**
	 * The client the invite belongs to.
	 */
	private final IDiscordClient client;

	/**
	 * The invite code (unique ID).
	 */
	private final String code;
	/**
	 * The ID of the guild the invite is for.
	 */
	private final long guildID;
	/**
	 * The ID of the channel the invite is for.
	 */
	private final long channelID;
	/**
	 * The ID of the user who created the invite.
	 */
	private final long inviterID;

	public Invite(IDiscordClient client, InviteObject backing) {
		this.client = client;
		this.code = backing.code;
		this.guildID = Long.parseUnsignedLong(backing.guild.id);
		this.channelID = Long.parseUnsignedLong(backing.channel.id);
		this.inviterID = backing.inviter == null ? 0 : Long.parseUnsignedLong(backing.inviter.id);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public IGuild getGuild() {
		return client.getGuildByID(guildID);
	}

	@Override
	public IChannel getChannel() {
		return getGuild().getChannelByID(channelID);
	}

	@Override
	public IUser getInviter() {
		return inviterID == 0 ? null : getGuild().getUserByID(inviterID);
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public void delete() {
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.INVITE + getCode());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IInvite) other).getCode().equals(getCode());
	}

	@Override
	public String toString() {
		return "discord.gg/" + getCode();
	}
}
