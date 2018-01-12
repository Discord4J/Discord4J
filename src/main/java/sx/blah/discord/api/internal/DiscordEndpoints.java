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

package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;

import static sx.blah.discord.api.internal.DiscordUtils.API_VERSION;

/**
 * Utility class containing constants for Discord API endpoints.
 */
public final class DiscordEndpoints {

	/**
	 * The base URL.
	 */
	public static final String BASE;

	static {
		if (Discord4J.alternateUrl != null) {
			BASE = Discord4J.alternateUrl;
		} else {
			BASE = "https://discordapp.com/";
		}
	}

	/**
	 * The base API URL.
	 */
	public static final String APIBASE = BASE+"api/v" + API_VERSION;

	/**
	 * The gateway endpoint.
	 */
	public static final String GATEWAY = APIBASE+"/gateway";

	/**
	 * The users endpoint.
	 */
	public static final String USERS = APIBASE+"/users/";

	/**
	 * The guilds endpoint.
	 */
	public static final String GUILDS = APIBASE+"/guilds/";

	/**
	 * The channels endpoint.
	 */
	public static final String CHANNELS = APIBASE+"/channels/";

	/**
	 * The webhooks endpoint.
	 */
	public static final String WEBHOOKS = APIBASE+"/webhooks/";

	/**
	 * The invite endpoint.
	 */
	public static final String INVITE = APIBASE+"/invite/";

	/**
	 * Formatted string for getting avatar URLs.
	 */
	public static final String AVATARS = "https://cdn.discordapp.com/avatars/%s/%s.%s";

	/**
	 * Formatted string for getting guild icon URLs.
	 */
	public static final String ICONS = "https://cdn.discordapp.com/icons/%s/%s.jpg";

	/**
	 * Formatted string for getting api metric information.
	 */
	public static final String METRICS = "https://srhpyqt94yxb.statuspage.io/metrics-display/d5cggll8phl5/%s.json";

	/**
	 * Formatted string for getting maintenance information.
	 */
	public static final String STATUS = "https://status.discordapp.com/api/v2/scheduled-maintenances/%s.json";

	/**
	 * The voice endpoint.
	 */
	public static final String VOICE = APIBASE+"/voice/";

	/**
	 * The OAuth2 url.
	 */
	public static final String OAUTH = APIBASE+"/oauth2/";

	/**
	 * The applications url.
	 */
	public static final String APPLICATIONS = OAUTH+"applications";

	/**
	 * Formatted string for getting application icons.
	 */
	public static final String APPLICATION_ICON = "https://cdn.discordapp.com/app-icons/%s/%s.jpg";

	/**
	 * The OAuth2 authorization url.
	 */
	public static final String AUTHORIZE = "https://discordapp.com/oauth2/authorize";

	/**
	 * Formatted string for getting emoji images.
	 */
	public static final String EMOJI_IMAGE = "https://cdn.discordapp.com/emojis/%s";

	/**
	 * The reactions list url for a message. Replacements in order: channel ID, message ID
	 */
	public static final String REACTIONS = CHANNELS + "%s/messages/%s/reactions";

	/**
	 * The reactions user list url. Replacements in order: channel ID, message ID, emoji (emoji or name:id)
	 */
	public static final String REACTIONS_USER_LIST = REACTIONS + "/%s";

	/**
	 * The reactions url for a specific user. Used for deleting/putting a reaction. Replacements in order: channel ID,
	 * message ID, emoji (emoji or name:id), user (@me or ID)
	 */
	public static final String REACTIONS_USER = REACTIONS_USER_LIST + "/%s";

	/**
	 * Default avatars url (arg 1 is <code>discrim % 5</code>)
	 */
	public static final String DEFAULT_AVATAR = "https://cdn.discordapp.com/embed/avatars/%d.png";

}
