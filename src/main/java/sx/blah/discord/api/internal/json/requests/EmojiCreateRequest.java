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

import sx.blah.discord.handle.obj.IRole;

import java.util.Arrays;

/**
 * Sent to create a new emoji.
 */
public class EmojiCreateRequest {

	/**
	 * The name of the emoji.
	 */
	public String name;

	/**
	 * The string of bytes of the emoji.
	 */
	public String image;

	/**
	 * The roles for which this emoji will be whitelisted.
	 */
	public String[] roles;

	public EmojiCreateRequest(String name, String image, IRole[] roles) {
		this.name = name;
		this.image = image;
		this.roles = Arrays.stream(roles)
				.map(IRole::getStringID)
				.toArray(String[]::new);
	}
}
