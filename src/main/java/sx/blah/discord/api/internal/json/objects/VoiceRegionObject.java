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

package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json voice region object.
 */
public class VoiceRegionObject {
	/**
	 * The ID of the region.
	 */
	public String id;
	/**
	 * The name of the region.
	 */
	public String name;
	/**
	 * An example of the hostname for the region.
	 */
	public String sample_hostname;
	/**
	 * An example of the port for the region.
	 */
	public int sample_port;
	/**
	 * Whether the region is vip-only.
	 */
	public boolean vip;
	/**
	 * Whether discord considers this region to be optimal for the guild.
	 */
	public boolean optimal;
}
