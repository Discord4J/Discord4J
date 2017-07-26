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

package sx.blah.discord.api.internal.json.requests;

/**
 * Sent to edit a guild's properties.
 */
public class GuildEditRequest {

	/**
	 * The new name of the guild.
	 */
	public String name;

	/**
	 * The new region of the guild.
	 */
	public String region;

	/**
	 * The new verification of the guild.
	 */
	public int verification_level;

	/**
	 * The new icon of the guild.
	 */
	public String icon;

	/**
	 * The new ID of the afk channel of the guild.
	 */
	public String afk_channel_id;

	/**
	 * The new afk timeout of the guild.
	 */
	public int afk_timeout;

	public GuildEditRequest(String name, String region, int verification_level, String icon, String afk_channel_id, int afk_timeout) {
		this.name = name;
		this.region = region;
		this.verification_level = verification_level;
		this.icon = icon;
		this.afk_channel_id = afk_channel_id;
		this.afk_timeout = afk_timeout;
	}
}
