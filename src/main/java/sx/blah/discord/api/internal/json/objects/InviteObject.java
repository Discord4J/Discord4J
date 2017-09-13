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
 * Represents a json invite object.
 */
public class InviteObject {

	/**
	 * The invite code (unique ID).
	 */
	public String code;
	/**
	 * The guild this invite is for.
	 */
	public InviteGuildObject guild;
	/**
	 * The channel this invite is for.
	 */
	public InviteChannelObject channel;
	/**
	 * The user who created the invite. Not present for vanity url invites or invites created for widgets.
	 */
	public UserObject inviter;

	/**
	 * Represents the parent guild of an invite.
	 */
	public static class InviteGuildObject {
		/**
		 * The ID of the guild.
		 */
		public String id;
		/**
		 * The name of the guild.
		 */
		public String name;
		/**
		 * The hash of the guild splash (or null).
		 */
		public String splash;
		/**
		 * The hash of the guild icon (or null).
		 */
		public String icon;
	}

	/**
	 * Represents the channel an invite is for.
	 */
	public static class InviteChannelObject {
		/**
		 * The ID of the channel.
		 */
		public String id;
		/**
		 * The name of the channel.
		 */
		public String name;
		/**
		 * The type of the channel. "text" or "voice"
		 */
		public String type;
	}
}
