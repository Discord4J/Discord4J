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

package sx.blah.discord.handle.obj;

/**
 * Represents a {@link sx.blah.discord.handle.impl.obj.User}'s voice state in a guild.
 */
public interface IVoiceState extends IIDLinkedObject {

	/**
	 * Gets the guild for this voice state.
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets the voice channel for this voice state.
	 * This is null if the user is not in a voice channel.
	 *
	 * @return The voice channel.
	 */
	IVoiceChannel getChannel();

	/**
	 * Gets the user for this voice state.
	 * @return The user.
	 */
	IUser getUser();

	/**
	 * Gets the session id for this voice state.
	 * Note: Probably not useful to you.
	 * @return The session id.
	 */
	String getSessionID();

	/**
	 * Whether the user represented by this voice state is deafened on the guild-level.
	 * @return Guild-level deaf state.
	 */
	boolean isDeafened();

	/**
	 * Whether the user represented by this voice state is muted on the guild-level.
	 * @return Guild-level mute state.
	 */
	boolean isMuted();

	/**
	 * Whether the user represented by this voice state has deafened themselves in their client.
	 * @return Client-level deaf state.
	 */
	boolean isSelfDeafened();

	/**
	 * Whether the user represented by this voice state has muted themselves in their client.
	 * @return Client-level mute state.
	 */
	boolean isSelfMuted();

	/**
	 * Whether the user represented by this voice state is muted by the bot user.
	 * @return User suppressed state.
	 */
	boolean isSuppressed();

	/**
	 * Gets the ID of the guild this relates to.
	 *
	 * @return The guild id.
	 */
	@Override
	default long getLongID() {
		return getGuild().getLongID();
	}
}
