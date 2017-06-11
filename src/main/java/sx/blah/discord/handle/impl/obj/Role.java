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

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public class Role implements IRole {

	/**
	 * Where the role should be displayed. -1 is @everyone, it is always last
	 */
	protected volatile int position;

	/**
	 * The permissions the role has.
	 */
	protected volatile EnumSet<Permissions> permissions;

	/**
	 * The role name
	 */
	protected volatile String name;

	/**
	 * Whether this role is managed via plugins like twitch
	 */
	protected volatile boolean managed;

	/**
	 * The role id
	 */
	protected volatile long id;

	/**
	 * Whether to display this role separately from others
	 */
	protected volatile boolean hoist;

	/**
	 * The DECIMAL format for the color
	 */
	protected volatile Color color;

	/**
	 * Whether you can @mention this role.
	 */
	protected volatile boolean mentionable;

	/**
	 * The guild this role belongs to
	 */
	protected volatile IGuild guild;

	public Role(int position, int permissions, String name, boolean managed, long id, boolean hoist, int color, boolean mentionable, IGuild guild) {
		this.position = position;
		this.permissions = Permissions.getAllowedPermissionsForNumber(permissions);
		this.name = name;
		this.managed = managed;
		this.id = id;
		this.hoist = hoist;
		this.color = new Color(color);
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
	 * Sets the CACHED enabled permissions.
	 *
	 * @param permissions The permissions number.
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
	 * @param name The name.
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
	 * Sets whether this role is hoisted in the CACHE.
	 *
	 * @param hoist True if hoisted, false if otherwise.
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
	 * @param color The color decimal number.
	 */
	public void setColor(int color) {
		this.color = new Color(color);
	}

	@Override
	public boolean isMentionable() {
		return mentionable || isEveryoneRole();
	}

	/**
	 * Sets whether this role is mentionable in the CACHE.
	 *
	 * @param mentionable True if mentionable, false if otherwise.
	 */
	public void setMentionable(boolean mentionable) {
		this.mentionable = mentionable;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

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
		return isMentionable() ? (isEveryoneRole() ? "@everyone" : "<@&"+id+">") : name;
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
