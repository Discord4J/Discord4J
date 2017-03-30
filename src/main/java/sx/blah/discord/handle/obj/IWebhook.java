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

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Defines a Discord webhook for a channel.
 */
public interface IWebhook extends IDiscordObject<IWebhook> {

	/**
	 * Gets the guild/server this webhook belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets the channel this webhook belongs to.
	 *
	 * @return The channel.
	 */

	IChannel getChannel();

	/**
	 * Gets the author of this webhook.
	 *
	 * @return The author.
	 */

	IUser getAuthor();

	/**
	 * Gets the default name of this webhook.
	 *
	 * @return The default name.
	 */

	String getDefaultName();

	/**
	 * Gets the default avatar of this webhook.
	 *
	 * @return The default avatar.
	 */

	String getDefaultAvatar();

	/**
	 * Gets the secure token of the webhook.
	 *
	 * @return The secure token.
	 */

	String getToken();

	/**
	 * Changes the default name of the user this webhook posts as.
	 *
	 * @param name The default name for the user.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */

	void changeDefaultName(String name);

	/**
	 * Changes the default avatar of the user this webhook posts as.
	 *
	 * @param avatar The default avatar, as encoded in base64. For URLs, use {@link Image#forUrl(String, String)} and {@link #changeDefaultAvatar(Image)}
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeDefaultAvatar(String avatar);

	/**
	 * Changes the default avatar of the user this webhook posts as.
	 *
	 * @param avatar The Image object to use as the avatar.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */

	void changeDefaultAvatar(Image avatar);

	/**
	 * Deletes this webhook.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * Checks to see if this webhook is deleted.
	 *
	 * @return True if this webhook is deleted.
	 */
	boolean isDeleted();
}
