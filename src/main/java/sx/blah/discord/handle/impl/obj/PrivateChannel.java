package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.HTTP429Exception;

import java.util.*;

public class PrivateChannel extends Channel implements IPrivateChannel {

	/**
	 * The recipient of this private channel.
	 */
	protected final IUser recipient;

	public PrivateChannel(IDiscordClient client, IUser recipient, String id) {
		super(client, recipient.getName(), id, null, null, 0, new HashMap<>(), new HashMap<>());
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
	public void removePermissionsOverride(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePermissionsOverride(IRole role) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, HTTP429Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
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
	public void changeName(String name) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void changePosition(int position) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void changeTopic(String topic) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String mention() {
		return recipient.mention();
	}

	@Override
	public IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		throw new UnsupportedOperationException();
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

	@Override
	public String toString() {
		return recipient.toString();
	}

	@Override
	public IPrivateChannel copy() {
		return new PrivateChannel(client, recipient, id);
	}
}
