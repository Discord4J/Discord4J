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
 * Represents a json game object.
 */
public class GameObject {

	private static class Type {
		/**
		 * The GameObject type integer for playing a game.
		 */
		private static final int GAME = 0;
		/**
		 * The GameObject type integer for streaming.
		 */
		private static final int STREAMING = 1;
		/**
		 * The GameObject type integer for listening to something.
		 */
		private static final int LISTENING = 2;
		/**
		 * The GameObject type integer for watching something.
		 */
		private static final int WATCHING = 3;
	}

	/**
	 * The type of the game object.
	 *
	 * @see Type
	 */
	public int type;
	/**
	 * The name of the game.
	 */
	public String name;
	/**
	 * The streaming url.
	 */
	public String url;

	public GameObject() {}

	public GameObject(String name, String url) {
		this.name = name;
		this.url = url;
		this.type = url == null ? Type.GAME : Type.STREAMING;
	}

	public GameObject(String name, int type) {
		this.name = name;
		this.type = type;
	}
}
