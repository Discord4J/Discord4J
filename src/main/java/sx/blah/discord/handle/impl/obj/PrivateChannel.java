/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.HTTP403Exception;

import java.util.*;

public class PrivateChannel extends Channel implements IPrivateChannel {
	
	/**
	 * The recipient of this private channel.
	 */
	protected final IUser recipient;
	
	public PrivateChannel(IDiscordClient client, IUser recipient, String id) {
		this(client, recipient, id, new ArrayList<>());
	}
	
	public PrivateChannel(IDiscordClient client, IUser recipient, String id, List<IMessage> messages) {
		super(client, recipient.getName(), id, null, null, 0, messages, new HashMap<>(), new HashMap<>());
		this.recipient = recipient;
		this.isPrivate = true;
	}
	
	@Override
	public Map<String, PermissionOverride> getUserOverrides() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Map<String, PermissionOverride> getRoleOverrides() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addUserOverride(String userId, PermissionOverride override) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addRoleOverride(String roleId, PermissionOverride override) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void removePermissionsOverride(String id) throws HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void overrideRolePermissions(String roleID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void overrideUserPermissions(String userID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void delete() throws HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setPosition(int position) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getPosition() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String mention() {
		return recipient.mention();
	}
	
	@Override
	public void setTopic(String topic) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getTopic() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IGuild getGuild() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IGuild getParent() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getName() {
		return recipient.getName();
	}
	
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IUser getRecipient() {
		return recipient;
	}
}
