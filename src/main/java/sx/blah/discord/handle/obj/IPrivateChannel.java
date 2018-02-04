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

import java.util.EnumSet;
import java.util.List;

/**
 * A private channel with another user.
 *
 * <p>Some methods from {@link IChannel}, when called, will always throw an exception due to the incompatible nature
 * between a <i>guild</i> text channel (what IChannel typically represents) and a <i>private</i> text channel.
 * All deprecated methods defined by this interface will throw an exception if invoked and should be avoided.
 */
public interface IPrivateChannel extends IChannel {

	/**
	 * Gets the the recipient user of the channel.
	 *
	 * @return The recipient user of the channel.
	 */
	IUser getRecipient();

	/**
	 * {@inheritDoc}
	 */
	IPrivateChannel copy();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void edit(String name, int position, String topic);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	List<IExtendedInvite> getExtendedInvites();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	EnumSet<Permissions> getModifiedPermissions(IRole role);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void removePermissionsOverride(IUser user);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void removePermissionsOverride(IRole role);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void delete();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	int getPosition();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void changeName(String name);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void changePosition(int position);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	void changeTopic(String topic);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IExtendedInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	String getTopic();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IGuild getGuild();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	List<IWebhook> getWebhooks();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IWebhook getWebhookByID(long id);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name, Image avatar);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name, String avatar);

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	boolean isDeleted();

	/**
	 * @deprecated See {@link IPrivateChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a private channel.
	 */
	@Override
	@Deprecated
	boolean isNSFW();
}
