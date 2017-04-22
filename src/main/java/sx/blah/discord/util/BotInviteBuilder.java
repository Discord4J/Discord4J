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

package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;

/**
 * This is a utility class to build invite links for server owners to invite your bot.
 */
public class BotInviteBuilder {

	private final IDiscordClient client;
	private IGuild guild;
	private EnumSet<Permissions> permissions;
	private String clientIDOverride;

	public BotInviteBuilder(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * This makes the invite link specific to the given guild.
	 *
	 * @param guild The guild for this invite link.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withGuild(IGuild guild) {
		this.guild = guild;
		return this;
	}

	/**
	 * This replaces the client id provided by your {@link IDiscordClient} instance.
	 *
	 * @param id The client id to override with.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withClientID(String id) {
		this.clientIDOverride = id;
		return this;
	}

	/**
	 * This makes the invite link request specific permissions for the bot when it joins.
	 *
	 * @param permissions The permissions to request.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withPermissions(EnumSet<Permissions> permissions) {
		this.permissions = permissions;
		return this;
	}

	/**
	 * Builds the actual invite link.
	 *
	 * @return The invite link.
	 */
	public String build() {
		String url = DiscordEndpoints.AUTHORIZE+"?client_id=%s&scope=bot";

		if (permissions != null)
			url += "&permissions="+Permissions.generatePermissionsNumber(permissions);

		if (guild != null)
			url += "&guild_id="+guild.getStringID();

		try {
			return String.format(url, clientIDOverride == null ? client.getApplicationClientID() : clientIDOverride);
		} catch (DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
		}
		return null;
	}

	/**
	 * Wrapper for {@link #build()}.
	 *
	 * @return The completed invite link.
	 */
	@Override
	public String toString() {
		return build();
	}
}
