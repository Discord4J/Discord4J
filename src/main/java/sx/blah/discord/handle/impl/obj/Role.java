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

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.RoleObject;
import sx.blah.discord.api.internal.json.requests.RoleEditRequest;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.PermissionUtils;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

/**
 * The default implementation of {@link IRole}.
 */
public class Role implements IRole {

	/**
	 * The position of the role.
	 */
	protected volatile int position;

	/**
	 * The permissions granted to the role.
	 */
	protected volatile EnumSet<Permissions> permissions;

	/**
	 * The name of the role.
	 */
	protected volatile String name;

	/**
	 * Whether the role is managed by an external service.
	 */
	protected volatile boolean managed;

	/**
	 * The unique snowflake ID of the role.
	 */
	protected volatile long id;

	/**
	 * Whether the role is hoisted.
	 */
	protected volatile boolean hoist;

	/**
	 * The color of the role.
	 */
	protected volatile Color color;

	/**
	 * Whether the role is mentionable.
	 */
	protected volatile boolean mentionable;

	/**
	 * The parent guild of the role.
	 */
	protected volatile IGuild guild;

	public Role(int position, int permissions, String name, boolean managed, long id, boolean hoist, int color, boolean mentionable, IGuild guild) {
		this.position = position;
		this.permissions = Permissions.getAllowedPermissionsForNumber(permissions);
		this.name = name;
		this.managed = managed;
		this.id = id;
		this.hoist = hoist;
		this.color = new Color(color, true);
		this.mentionable = mentionable;
		this.guild = guild;
	}

	@Override
	public int getPosition() {
		return getGuild().getRoles().indexOf(this);
	}

	/**
	 * Sets the CACHED role position.
	 *
	 * @param position The role position.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public EnumSet<Permissions> getPermissions() {
		return permissions.clone();
	}

	/**
	 * Sets the CACHED permissions by the raw permissions number.
	 *
	 * @param permissions The raw permissions number.
	 */
	public void setPermissions(int permissions) {
		this.permissions = Permissions.getAllowedPermissionsForNumber(permissions);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the CACHED role name.
	 *
	 * @param name The role name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isManaged() {
		return managed;
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public boolean isHoisted() {
		return hoist;
	}

	/**
	 * Sets the CACHED hoist value.
	 *
	 * @param hoist The hoist value.
	 */
	public void setHoist(boolean hoist) {
		this.hoist = hoist;
	}

	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the CACHED role color.
	 *
	 * @param color The role color.
	 */
	public void setColor(int color) {
		this.color = new Color(color, true);
	}

	@Override
	public boolean isMentionable() {
		return mentionable || isEveryoneRole();
	}

	/**
	 * Sets the CACHED mentionable value.
	 *
	 * @param mentionable The mentionable value.
	 */
	public void setMentionable(boolean mentionable) {
		this.mentionable = mentionable;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	/**
	 * Sends a request to edit the role.
	 *
	 * @param request The request object describing the changes to make.
	 */
	private void edit(RoleEditRequest request) {
		PermissionUtils.requireHierarchicalPermissions(guild, getClient().getOurUser(), Collections.singletonList(this), Permissions.MANAGE_ROLES);

		try {
			DiscordUtils.getRoleFromJSON(guild,
					DiscordUtils.MAPPER.readValue(
							((DiscordClientImpl) getClient()).REQUESTS.PATCH.makeRequest(
									DiscordEndpoints.GUILDS + guild.getStringID() + "/roles/" + id,
									DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(request)), RoleObject.class));
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(Color color, boolean hoist, String name, EnumSet<Permissions> permissions, boolean isMentionable) {
		if (color == null)
			throw new IllegalArgumentException("Color must not be null.");
		if (name == null || name.length() < 1 || name.length() > 32)
			throw new IllegalArgumentException("Role name must be between 1 and 32 characters!");
		if (permissions == null)
			throw new IllegalArgumentException("Permissions set must not be null.");

		edit(new RoleEditRequest.Builder().color(color).hoist(hoist).name(name).permissions(permissions).mentionable(isMentionable).build());
	}

	@Override
	public void changeColor(Color color) {
		if (color == null)
			throw new IllegalArgumentException("Color must not be null.");

		edit(new RoleEditRequest.Builder().color(color).build());
	}

	@Override
	public void changeHoist(boolean hoist) {
		edit(new RoleEditRequest.Builder().mentionable(hoist).build());
	}

	@Override
	public void changeName(String name) {
		if (name == null || name.length() < 1 || name.length() > 32)
			throw new IllegalArgumentException("Role name must be between 1 and 32 characters!");

		edit(new RoleEditRequest.Builder().name(name).build());
	}

	@Override
	public void changePermissions(EnumSet<Permissions> permissions) {
		if (permissions == null)
			throw new IllegalArgumentException("Permissions set must not be null.");

		edit(new RoleEditRequest.Builder().permissions(permissions).build());
	}

	@Override
	public void changeMentionable(boolean mentionable) {
		edit(new RoleEditRequest.Builder().mentionable(mentionable).build());
	}

	@Override
	public void delete() {
		PermissionUtils.requireHierarchicalPermissions(guild, getClient().getOurUser(), Collections.singletonList(this), Permissions.MANAGE_ROLES);

		((DiscordClientImpl) getClient()).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+guild.getStringID()+"/roles/"+id);
	}

	@Override
	public IRole copy() {
		return new Role(position, Permissions.generatePermissionsNumber(permissions), name, managed, id, hoist,
				color.getRGB(), mentionable, guild);
	}

	@Override
	public IDiscordClient getClient() {
		return getGuild().getClient();
	}

	@Override
	public IShard getShard() {
		return getGuild().getShard();
	}

	@Override
	public boolean isEveryoneRole() {
		return guild.getEveryoneRole().equals(this);
	}

	@Override
	public boolean isDeleted() {
		return getGuild().getRoleByID(id) != this;
	}

	@Override
	public String mention() {
		return isEveryoneRole() ? "@everyone" : "<@&"+id+">";
	}

	@Override
	public String toString() {
		return mention();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		return DiscordUtils.equals(this, other);
	}
}
