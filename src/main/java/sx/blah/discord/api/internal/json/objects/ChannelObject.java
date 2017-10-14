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
 * Represents a json channel object.
 */
public class ChannelObject {

	public String id;
	public int type;
	public String guild_id;
	public Integer position;
	public OverwriteObject[] permission_overwrites;
	public String name;
	public String topic;
	public String last_message_id;
	public Integer bitrate;
	public Integer user_limit;
	public UserObject[] recipients;
	public String icon;
	public String owner_id;
	public String application_id;
	public boolean nsfw;
	public String parent_id;

	public static class Type {
		public static final int GUILD_TEXT = 0;
		public static final int PRIVATE = 1;
		public static final int GUILD_VOICE = 2;
		public static final int GROUP_PRIVATE = 3;
		public static final int GUILD_CATEGORY = 4;
	}

}
