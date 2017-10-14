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
 * Builds OAuth2 bot invite URLs.
 */
public class BotInviteBuilder {

	/**
	 * The client the builder is making an invite for.
	 */
	private final IDiscordClient client;
	/**
	 * The guild that the invite is for.
	 */
	private IGuild guild;
	/**
	 * The permissions requested in the invite.
	 */
	private EnumSet<Permissions> permissions;
	/**
	 * The client ID for the invite. (Or null to use the client's)
	 */
	private String clientIDOverride;

	public BotInviteBuilder(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Sets the guild that the invite is for.
	 *
	 * @param guild The guild that the invite is for.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withGuild(IGuild guild) {
		this.guild = guild;
		return this;
	}

	/**
	 * Sets the client ID for the invite. This overrides the client ID from the given client.
	 *
	 * @param id The client ID for the invite.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withClientID(String id) {
		this.clientIDOverride = id;
		return this;
	}

	/**
	 * Sets the permissions requested in the invite.
	 *
	 * @param permissions The permissions requested in the invite.
	 * @return The builder instance.
	 */
	public BotInviteBuilder withPermissions(EnumSet<Permissions> permissions) {
		this.permissions = permissions;
		return this;
	}

	/**
	 * Builds the invite URL with the configuration specified by the builder.
	 *
	 * @return The invite URL with the configuration specified by the builder.
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
	 * @return The invite URL with the configuration specified by the builder.
	 */
	@Override
	public String toString() {
		return build();
	}
}
