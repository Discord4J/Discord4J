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

public class Invite implements IInvite {

	private final IDiscordClient client;
	private final InviteObject backing;

	public Invite(IDiscordClient client, InviteObject backing) {
		this.client = client;
		this.backing = backing;
	}

	@Override
	public String getCode() {
		return backing.code;
	}

	@Override
	public String getInviteCode() {
		return getCode();
	}

	@Override
	public IGuild getGuild() {
		return client.getGuildByID(backing.guild.id);
	}

	@Override
	public IChannel getChannel() {
		return getGuild().getChannelByID(backing.channel.id);
	}

	@Override
	public IUser getInviter() {
		return getGuild().getUserByID(backing.inviter.id);
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
	public InviteResponse details() {
		return null;
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
