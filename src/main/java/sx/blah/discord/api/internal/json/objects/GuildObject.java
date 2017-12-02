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
 * Represents a json guild object.
 */
public class GuildObject {
	/**
	 * The ID of the guild.
	 */
	public String id;
	/**
	 * The name of the guild.
	 */
	public String name;
	/**
	 * The icon of the guild.
	 */
	public String icon;
	/**
	 * The ID of the user who owns the guild.
	 */
	public String owner_id;
	/**
	 * The region the guild's voice server is in.
	 */
	public String region;
	/**
	 * The ID of the AFK voice channel.
	 */
	public String afk_channel_id;
	/**
	 * The timeout for moving people to the AFK voice channel.
	 */
	public int afk_timeout;
	/**
	 * Whether this guild is embeddable via a widget.
	 */
	public boolean embed_enabled;
	/**
	 * The ID of the embedded channel.
	 */
	public String embed_channel_id;
	/**
	 * The level of verification.
	 */
	public int verification_level;
	/**
	 * The default message notifications level.
	 */
	public int default_messages_notifications;
	/**
	 * Array of role objects.
	 */
	public RoleObject[] roles;
	/**
	 * Array of emoji objects.
	 */
	public EmojiObject[] emojis;
	/**
	 * Array of guild features.
	 */
	public String[] features;
	/**
	 * The required MFA level for the guild.
	 */
	public int mfa_level;
	/**
	 * The id of the channel to which system messages are sent.
	 */
	public String system_channel_id;
	/**
	 * The ISO-8601 timestamp of when our user joined the guild.
	 */
	public String joined_at;
	/**
	 * Whether the guild is considered to be large.
	 */
	public boolean large;
	/**
	 * Whether the guild is unavailable.
	 */
	public boolean unavailable;
	/**
	 * The number of members in the guild.
	 */
	public int member_count;
	/**
	 * Array of voice states for the members in the guild.
	 */
	public VoiceStateObject[] voice_states;
	/**
	 * Array of members.
	 */
	public MemberObject[] members;
	/**
	 * Array of channels.
	 */
	public ChannelObject[] channels;
	/**
	 * Array of presences for members in the guild.
	 */
	public PresenceObject[] presences;
}
