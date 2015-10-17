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

package sx.blah.discord.handle.obj;

import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import sx.blah.discord.Discord4J;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.DiscordEndpoints;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

/**
 * @author qt
 * @since 9:48 PM 17 Aug, 2015
 * Project: DiscordAPI
 */
public class Invite {
	/**
	 * An invite code, AKA an invite URL minus the https://discord.gg/
	 */
	private final String inviteCode;

	public Invite(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	/**
	 * @return The invite code
	 */
	public String getInviteCode() {
		return inviteCode;
	}

	/**
	 * @return Accepts the invite and returns relevant information,
	 *         such as the Guild ID and name, and the channel the invite
	 *         was created from.
	 * @throws Exception
	 */
	public void accept() throws HTTP403Exception {
		if (DiscordClient.get().isReady()) {
			String response = Requests.POST.makeRequest(DiscordEndpoints.INVITE + inviteCode,
					new BasicNameValuePair("authorization", DiscordClient.get().getToken()));
		} else {
			Discord4J.logger.error("Bot has not signed in yet!");
		}
	}

	/**
	 * Gains the same information as accepting,
	 * but doesn't actually accept the invite.
	 *
	 * @return an InviteResponse containing the invite's details.
	 * @throws Exception
	 */
	public InviteResponse details() throws Exception {
		if (DiscordClient.get().isReady()) {
			String response = Requests.GET.makeRequest(DiscordEndpoints.INVITE + inviteCode,
					new BasicNameValuePair("authorization", DiscordClient.get().getToken()));

			JSONObject object1 = (JSONObject) new JSONParser().parse(response);
			JSONObject guild = (JSONObject) object1.get("guild");
			JSONObject channel = (JSONObject) object1.get("channel");

			return new InviteResponse((String) guild.get("id"),
					(String) guild.get("name"),
					(String) channel.get("id"),
					(String) channel.get("name"));
		} else {
			Discord4J.logger.error("Bot has not signed in yet!");
			return null;
		}
	}

	public class InviteResponse {
		/**
		 * ID of the guild you were invited to.
		 */
		private final String guildID;

		/**
		 * Name of the guild you were invited to.
		 */
		private final String guildName;

		/**
		 * ID of the channel you were invited from.
		 */
		private final String channelID;

		/**
		 * Name of the channel you were invited from.
		 */
		private final String channelName;

		//TODO replace with objects. Need to figure out logistics, as the GUILD_CREATE is sent after MESSAGE_CREATE and after we accept the invite
		public InviteResponse(String guildID, String guildName, String channelID, String channelName) {
			this.guildID = guildID;
			this.guildName = guildName;
			this.channelID = channelID;
			this.channelName = channelName;
		}

		public String getGuildID() {
			return guildID;
		}

		public String getGuildName() {
			return guildName;
		}

		public String getChannelID() {
			return channelID;
		}

		public String getChannelName() {
			return channelName;
		}
	}
}
