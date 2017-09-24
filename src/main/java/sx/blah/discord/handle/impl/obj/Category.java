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
import sx.blah.discord.api.internal.json.objects.OverwriteObject;
import sx.blah.discord.api.internal.json.requests.ChannelEditRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Category implements ICategory {

	private final DiscordClientImpl client;
	private final IShard shard;
	private volatile String name;
	private final long id;
	private volatile IGuild guild;
	private volatile int position;
	private volatile boolean nsfw;

	private final Cache<PermissionOverride> userOverrides;
	private final Cache<PermissionOverride> roleOverrides;

	public Category(IShard shard, String name, long id, IGuild guild, int position, boolean nsfw, Cache<PermissionOverride> userOverrides, Cache<PermissionOverride> roleOverrides) {
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
		this.name = name;
		this.guild = guild;
		this.position = position;
		this.id = id;
		this.nsfw = nsfw;
		this.userOverrides = userOverrides;
		this.roleOverrides = roleOverrides;
	}

	@Override
	public void delete() {
		PermissionUtils.requirePermissions(getModifiedPermissions(getClient().getOurUser()), EnumSet.of(Permissions.MANAGE_CHANNEL));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+id);
	}

	@Override
	public boolean isDeleted() {
		return getClient().getCategoryById(id) != this;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public int getPosition() {
		return position;
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
		if (name == null || !name.matches("^[a-z0-9-_]{2,100}$"))
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric characters.");

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
		return nsfw;
	}

	@Override
	public ICategory copy() {
		return new Category(shard, name, id, guild, position, nsfw, userOverrides, roleOverrides);
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public List<IChannel> getChannels() {
		return getGuild().getChannels().stream()
				.filter(channel -> channel.getCategory().equals(this))
				.collect(Collectors.toList());
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		if (getGuild().getOwnerLongID() == user.getLongID())
			return EnumSet.allOf(Permissions.class);

		List<IRole> roles = user.getRolesForGuild(guild);
		EnumSet<Permissions> permissions = user.getPermissionsForGuild(guild);

		if (!permissions.contains(Permissions.ADMINISTRATOR)) {
			PermissionOverride override = userOverrides.get(user.getLongID());
			List<PermissionOverride> overrideRoles = roles.stream()
					.filter(r -> roleOverrides.containsKey(r.getLongID()))
					.map(role -> roleOverrides.get(role.getLongID()))
					.collect(Collectors.toList());
			Collections.reverse(overrideRoles);
			for (PermissionOverride roleOverride : overrideRoles) {
				permissions.addAll(roleOverride.allow());
				permissions.removeAll(roleOverride.deny());
			}

			if (override != null) {
				permissions.addAll(override.allow());
				permissions.removeAll(override.deny());
			}
		}

		return permissions;
	}

	@Override
	public LongMap<PermissionOverride> getUserOverridesLong() {
		return userOverrides.mapCopy();
	}

	@Override
	public LongMap<PermissionOverride> getRoleOverridesLong() {
		return roleOverrides.mapCopy();
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		EnumSet<Permissions> base = role.getPermissions();
		PermissionOverride override = roleOverrides.get(role.getLongID());

		if (override == null) {
			if ((override = roleOverrides.get(guild.getEveryoneRole().getLongID())) == null)
				return base;
		}

		base.addAll(new ArrayList<>(override.allow()));
		override.deny().forEach(base::remove);

		return base;
	}

	@Override
	public void removePermissionsOverride(IUser user) {
		PermissionUtils.requirePermissions(getModifiedPermissions(getClient().getOurUser()), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+user.getStringID());

		userOverrides.remove(user.getLongID());
	}

	@Override
	public void removePermissionsOverride(IRole role) {
		// TODO Require hierarchical permissions

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
