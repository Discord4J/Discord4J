/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal.json.responses;

/**
 * The response when the emoji list for a guild updates.
 */
public class GuildEmojiUpdateResponse {

	/**
	 * The guild involved.
	 */
	public String guild_id;

	/**
	 * The emoji objects.
	 */
	public EmojiObj[] emojis;

	public static class EmojiObj {

		/**
		 * Array of role IDs.
		 */
		public String[] roles;

		/**
		 * If the emoji needs colons :X:
		 */
		public boolean require_colons;

		/**
		 * The emoji name.
		 */
		public String name;

		/**
		 * If the emoji is externally managed by an integration.
		 */
		public boolean managed;

		/**
		 * The emoji's ID.
		 */
		public String id;

	}

}
