/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.data;

public class OverwriteData {

	private final long id;
	private final String type;
	private final int allow;
	private final int deny;

	public OverwriteData(long id, String type, int allow, int deny) {
		this.id = id;
		this.type = type;
		this.allow = allow;
		this.deny = deny;
	}

	public long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public int getAllow() {
		return allow;
	}

	public int getDeny() {
		return deny;
	}
}
