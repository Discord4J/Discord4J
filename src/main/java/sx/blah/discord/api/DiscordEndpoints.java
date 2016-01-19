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

package sx.blah.discord.api;

/**
 * Static class that contains
 * URLs useful to us.
 */
public final class DiscordEndpoints {
	/**
	 * The base URL.
	 */
	public static final String BASE = "https://discordapp.com/";
	/**
	 * The base API location on Discord's servers.
	 */
	public static final String APIBASE = BASE+"api";
	
	public static final String USERS = APIBASE+"/users/";
	
	/**
	 * Used for logging in.
	 */
	public static final String LOGIN = APIBASE+"/auth/login";
	/**
	 * Used for logging out.
	 */
	public static final String LOGOUT = APIBASE+"/auth/logout";
	
	/**
	 * Servers URL
	 */
	public static final String SERVERS = APIBASE+"/guilds/";
	
	public static final String CHANNELS = APIBASE+"/channels/";
	
	/**
	 * Used for accepting invites
	 */
	public static final String INVITE = APIBASE+"/invite/";
	
	/**
	 * Formatted string for getting avatar URLs.
	 */
	public static final String AVATARS = "https://cdn.discordapp.com/avatars/%s/%s.jpg";
	
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
	 * Voice url.
	 */
	public static final String VOICE = APIBASE + "/voice/";
}
