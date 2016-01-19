/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle.impl.obj;

import com.google.gson.Gson;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.json.responses.InviteJSONResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

public class Invite implements IInvite {
	/**
	 * An invite code, AKA an invite URL minus the https://discord.gg/
	 */
	protected final String inviteCode;
	
	/**
	 * The human-readable version of the invite code, if available.
	 */
	protected final String xkcdPass;
	
	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;
	
	public Invite(IDiscordClient client, String inviteCode, String xkcdPass) {
		this.client = client;
		this.inviteCode = inviteCode;
		this.xkcdPass = xkcdPass;
	}
	
	@Override
	public String getInviteCode() {
		return inviteCode;
	}
	
	@Override
	public String getXkcdPass() {
		return xkcdPass;
	}
	
	@Override
	public InviteResponse accept() throws Exception {
		if (client.isReady()) {
			String response = Requests.POST.makeRequest(DiscordEndpoints.INVITE+inviteCode,
					new BasicNameValuePair("authorization", client.getToken()));
			
			InviteJSONResponse inviteResponse = new Gson().fromJson(response, InviteJSONResponse.class);
			
			return new InviteResponse(inviteResponse.guild.id, inviteResponse.guild.name,
					inviteResponse.channel.id, inviteResponse.channel.name);
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}
	
	@Override
	public InviteResponse details() throws Exception {
		if (client.isReady()) {
			String response = Requests.GET.makeRequest(DiscordEndpoints.INVITE+inviteCode,
					new BasicNameValuePair("authorization", client.getToken()));
			
			InviteJSONResponse inviteResponse = new Gson().fromJson(response, InviteJSONResponse.class);
			
			return new InviteResponse(inviteResponse.guild.id, inviteResponse.guild.name,
					inviteResponse.channel.id, inviteResponse.channel.name);
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}
	
	@Override
	public void delete() throws HTTP403Exception {
		Requests.DELETE.makeRequest(DiscordEndpoints.INVITE+inviteCode,
				new BasicNameValuePair("authorization", client.getToken()));
	}
	
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((IInvite) other).getInviteCode().equals(getInviteCode());
	}
}
