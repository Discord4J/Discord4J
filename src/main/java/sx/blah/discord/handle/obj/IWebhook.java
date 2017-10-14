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

import sx.blah.discord.util.Image;

/**
 * A webhook in a guild text {@link IChannel channel}.
 */
public interface IWebhook extends IDiscordObject<IWebhook> {

	/**
	 * Gets the parent guild of the webhook.
	 *
	 * @return The parent guild of the webhook.
	 */
	IGuild getGuild();

	/**
	 * Gets the parent channel of the webhook.
	 *
	 * @return The parent channel lof the webhook.
	 */
	IChannel getChannel();

	/**
	 * Gets the user who created the webhook.
	 *
	 * @return The user who created the webhook.
	 */
	IUser getAuthor();

	/**
	 * Gets the webhook's default name.
	 *
	 * @return The webhook's default name.
	 */
	String getDefaultName();

	/**
	 * Gets the webhook's default avatar.
	 *
	 * @return The webhook's default avatar.
	 */
	String getDefaultAvatar();

	/**
	 * Gets the webhook's secure token.
	 *
	 * @return The webhook's secure token.
	 */
	String getToken();

	/**
	 * Changes the default name of the webhook.
	 *
	 * @param name The default name of the webhook.
	 */
	void changeDefaultName(String name);

	/**
	 * Changes the default avatar of the webhook.
	 *
	 * @param avatar The base64-encoded default avatar of the webhook.
	 */
	void changeDefaultAvatar(String avatar);

	/**
	 * Changes the default avatar of the webhook.
	 *
	 * @param avatar The default avatar of the webhook.
	 */
	void changeDefaultAvatar(Image avatar);

	/**
	 * Deletes this webhook.
	 */
	void delete();

	/**
	 * Gets whether the webhook is deleted.
	 *
	 * @return Whether the webhook is deleted.
	 */
	boolean isDeleted();
}
