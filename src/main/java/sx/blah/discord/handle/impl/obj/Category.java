/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package sx.blah.discord.handle.impl.obj;

import com.fasterxml.jackson.core.JsonProcessingException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.ChannelObject;
import sx.blah.discord.api.internal.json.objects.OverwriteObject;
import sx.blah.discord.api.internal.json.requests.ChannelCreateRequest;
import sx.blah.discord.api.internal.json.requests.ChannelEditRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Category implements ICategory {

	protected final DiscordClientImpl client;
	protected final IShard shard;
	protected volatile String name;
	protected final long id;
	protected final IGuild guild;
	protected volatile int position;
	protected volatile boolean isNSFW;

	public final Cache<PermissionOverride> userOverrides;
	public final Cache<PermissionOverride> roleOverrides;

	public Category(IShard shard, String name, long id, IGuild guild, int position, boolean isNSFW, Cache<PermissionOverride> userOverrides, Cache<PermissionOverride> roleOverrides) {
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
		this.name = name;
		this.guild = guild;
		this.position = position;
		this.id = id;
		this.isNSFW = isNSFW;
		this.userOverrides = userOverrides;
		this.roleOverrides = roleOverrides;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setNSFW(boolean isNSFW) {
		this.isNSFW = isNSFW;
	}

	@Override
	public void delete() {
		PermissionUtils.requirePermissions(getModifiedPermissions(getClient().getOurUser()), EnumSet.of(Permissions.MANAGE_CHANNEL));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+id);
	}

	@Override
	public boolean isDeleted() {
		return getClient().getCategoryByID(id) != this;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public int getPosition() {
		return getGuild().getCategories().indexOf(this);
	}

	@Override
	public void changePosition(int position) {
		edit(new ChannelEditRequest.Builder().position(position).build());
	}

	private void edit(ChannelEditRequest request) {
		PermissionUtils.requirePermissions(getModifiedPermissions(client.getOurUser()), EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		try {
			client.REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.CHANNELS + id,
					DiscordUtils.MAPPER.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void changeName(String name) {
		if (name == null || name.length() < 2 || name.length() > 100)
			throw new IllegalArgumentException("Category name must be between 2 and 100 characters!");

		edit(new ChannelEditRequest.Builder().name(name).build());
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		return shard;
	}

	@Override
	public boolean isNSFW() {
		return isNSFW;
	}

	@Override
	public void changeNSFW(boolean nsfw) {
		edit(new ChannelEditRequest.Builder().nsfw(isNSFW).build());
	}

	@Override
	public ICategory copy() {
		return new Category(shard, name, id, guild, position, isNSFW, userOverrides.copy(), roleOverrides.copy());
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public List<IChannel> getChannels() {
		return getGuild().getChannels().stream()
				.filter(channel -> equals(channel.getCategory()))
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return getGuild().getVoiceChannels().stream()
				.filter(channel -> equals(channel.getCategory()))
				.collect(Collectors.toList());
	}

	@Override
	public IChannel createChannel(String name) {
		if (name == null || !DiscordUtils.CHANNEL_NAME_PATTERN.matcher(name).matches())
			throw new DiscordException("Channel name must be 2-100 alphanumeric OR non-ASCII characters.");

		return ((Guild) guild).createChannel(new ChannelCreateRequest(name, ChannelObject.Type.GUILD_TEXT, id));
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) {
		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name must be between 2 and 100 characters!");

		return (IVoiceChannel) ((Guild) guild).createChannel(new ChannelCreateRequest(name, ChannelObject.Type.GUILD_VOICE, id));
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		return PermissionUtils.getModifiedPermissions(user, guild, userOverrides, roleOverrides);
	}

	@Override
	public LongMap<PermissionOverride> getUserOverrides() {
		return userOverrides.mapCopy();
	}

	@Override
	public LongMap<PermissionOverride> getRoleOverrides() {
		return roleOverrides.mapCopy();
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		return PermissionUtils.getModifiedPermissions(role, roleOverrides);
	}

	@Override
	public void removePermissionsOverride(IUser user) {
		PermissionUtils.requirePermissions(getModifiedPermissions(getClient().getOurUser()), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+user.getStringID());

		userOverrides.remove(user.getLongID());
	}

	@Override
	public void removePermissionsOverride(IRole role) {
		PermissionUtils.requireHierarchicalPermissions(getGuild(), client.getOurUser(), Collections.singletonList(role), Permissions.MANAGE_PERMISSIONS);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+role.getStringID());

		roleOverrides.remove(role.getLongID());
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		overridePermissions("role", role.getStringID(), toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		overridePermissions("member", user.getStringID(), toAdd, toRemove);
	}

	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		PermissionUtils.requirePermissions(getModifiedPermissions(getClient().getOurUser()), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
				DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+id,
				new OverwriteObject(type, null, Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)));
	}

	@Override
	public int hashCode() {
		return Long.hashCode(getLongID());
	}

	@Override
	public boolean equals(Object obj) {
		return DiscordUtils.equals(this, obj);
	}
}
