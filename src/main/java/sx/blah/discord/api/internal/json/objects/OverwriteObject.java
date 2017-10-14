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
 * Represents a json permission overwrite object.
 */
public class OverwriteObject {
	/**
	 * The ID of the overwrite.
	 */
	public String id;
	/**
	 * The type of the overwrite.
	 */
	public String type;
	/**
	 * The permissions explicitly allowed by this overwrite.
	 */
	public int allow;
	/**
	 * The permissions explicitly denied by this overwrite.
	 */
	public int deny;

	public OverwriteObject() {}

	public OverwriteObject(String type, String id, int allow, int deny) {
		this.id = id;
		this.type = type;
		this.allow = allow;
		this.deny = deny;
	}
}
