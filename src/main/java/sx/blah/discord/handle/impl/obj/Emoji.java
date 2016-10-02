/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Emoji implements IEmoji {

	/**
	 * The guild.
	 */
	protected final IGuild guild;

	/**
	 * The ID.
	 */
	protected final String id;
	/**
	 * Roles for integration?
	 */
	protected final List<IRole> roles;
	/**
	 * The name.
	 */
	protected volatile String name;
	/**
	 * If it requires colons :X:
	 */
	protected volatile boolean requiresColons;
	/**
	 * If it's managed externally.
	 */
	protected volatile boolean isManaged;

	public Emoji(IGuild guild, String id, String name, boolean requiresColons, boolean isManaged, IRole[] roles) {
		this(guild, id, name, requiresColons, isManaged, convertRolesToIDs(roles));
	}

	public Emoji(IGuild guild, String id, String name, boolean requiresColons, boolean isManaged, String[] roleIds) {
		this.guild = guild;
		this.id = id;
		this.name = name;
		this.requiresColons = requiresColons;
		this.isManaged = isManaged;
		this.roles = new CopyOnWriteArrayList<>();

		for (String roleId : roleIds) {
			IRole role = guild.getRoleByID(roleId);

			if (role != null) {
				this.roles.add(role);
			}
		}
	}

	private static String[] convertRolesToIDs(IRole[] roles) {
		List<String> ids = new ArrayList<>();

		for (IRole r : roles) {
			if (r == null)
				continue;

			ids.add(r.getID());
		}

		return ids.toArray(new String[ids.size()]);
	}

	public void setRequiresColons(boolean requiresColons) {
		this.requiresColons = requiresColons;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public IDiscordClient getClient() {
		return getGuild().getClient();
	}

	@Override
	public IEmoji copy() {
		Emoji copy = new Emoji(guild, id, name, requiresColons, isManaged, roles.toArray(new IRole[roles.size()]));

		return copy;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public boolean requiresColons() {
		return requiresColons;
	}

	@Override
	public boolean isManaged() {
		return isManaged;
	}

	public void setManaged(boolean managed) {
		isManaged = managed;
	}

	@Override
	public List<IRole> getRoles() {
		return roles;
	}

	@Override
	public String getImageUrl() {
		return "https://cdn.discordapp.com/emojis/" + getID() + ".png";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IGuild) other).getID().equals(getID());
	}

	@Override
	public String toString() {
		return "<:" + getName() + ":" + getID() + ">";
	}
}
