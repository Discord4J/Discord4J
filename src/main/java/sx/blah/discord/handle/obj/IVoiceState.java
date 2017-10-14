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
 * A {@link IUser}'s voice state in a {@link IGuild}.
 */
public interface IVoiceState extends IIDLinkedObject {

	/**
	 * Gets the guild of the voice state.
	 *
	 * @return The guild of the voice state.
	 */
	IGuild getGuild();

	/**
	 * Gets the voice channel of the voice state.
	 *
	 * @return The voice channel of the voice state (or null if the user is not in a voice channel).
	 */
	IVoiceChannel getChannel();

	/**
	 * Gets the user of the voice state.
	 *
	 * @return The user of the voice state.
	 */
	IUser getUser();

	/**
	 * Gets the session ID of the voice state.
	 *
	 * @return The session ID of the voice state.
	 */
	String getSessionID();

	/**
	 * Gets whether the user of the voice state is deafened on the guild.
	 *
	 * @return Whether the user of the voice state is deafened on the guild.
	 */
	boolean isDeafened();

	/**
	 * Gets whether the user of the voice state is muted on the guild.
	 *
	 * @return Whether the user of the voice state is muted on the guild.
	 */
	boolean isMuted();

	/**
	 * Gets whether the user of the voice state is deafened on the client.
	 *
	 * @return Whether the user of the voice state is deafened on the client.
	 */
	boolean isSelfDeafened();

	/**
	 * Gets whether the user of the voice state is muted on the client.
	 *
	 * @return Whether the user of the voice state is muted on the client.
	 */
	boolean isSelfMuted();

	/**
	 * Gets whether the user of the voice state is muted by the bot user.
	 *
	 * @return Whether the user of the voice state is muted by the bot user.
	 */
	boolean isSuppressed();

	/**
	 * Gets the ID of the guild of the voice state.
	 *
	 * @return The ID of the guild of the voice state.
	 */
	@Override
	default long getLongID() {
		return getGuild().getLongID();
	}
}
