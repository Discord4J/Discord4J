package sx.blah.discord.util;

import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.awt.*;
import java.util.EnumSet;

/**
 * Utility class designed to make the creation of roles easier.
 */
public class RoleBuilder {

	private IGuild guild;
	private Color color;
	private boolean hoist = false;
	private boolean mentionable = false;
	private String name;
	private EnumSet<Permissions> permissions;

	public RoleBuilder(IGuild guild) {
		this.guild = guild;
	}

	/**
	 * Sets the color of the role.
         *
	 * @param color The color of the role.
	 * @return The role builder instance
	 */
	public RoleBuilder withColor(Color color) {
		this.color = color;
		return this;
	}

	/**
	 * Determines whether or not this role is hoisted.
         *
	 * @param hoist If true, users with this role will be displayed separately from the "everyone" role.
	 * @return The role builder instance.
	 */
	public RoleBuilder setHoist(boolean hoist) {
		this.hoist = hoist;
		return this;
	}

	/**
	 * Determines whether or not this role is mentionable.
         *
	 * @param mentionable If true, users can use a role mention for this role.
	 * @return The role builder instance.
	 */
	public RoleBuilder setMentionable(boolean mentionable) {
		this.mentionable = mentionable;
		return this;
	}

	/**
	 * Sets the name of the role.
         *
	 * @param name The name of the role.
	 * @return The role builder instance.
	 */
	public RoleBuilder withName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the permissions of the role.
         *
	 * @param permissions A set of the permissions of the role.
	 * @return The role builder instance.
	 */
	public RoleBuilder withPermissions(EnumSet<Permissions> permissions) {
		this.permissions = permissions;
		return this;
	}

	/**
	 * Creates the role in the specified guild.
         *
	 * @return The {@link IRole} object representing the new role.
         *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	public IRole build() throws DiscordException, RateLimitException, MissingPermissionsException {
		if (guild == null)
			throw new RuntimeException("A guild must be set to create a role.");

		Role role = (Role) guild.createRole();
		role.edit(color != null ? color : role.getColor(),
				hoist,
				name != null ? name : role.getName(),
				permissions != null ? permissions : role.getPermissions(),
				mentionable);
		return role;
	}
}
