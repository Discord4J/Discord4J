package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.Arrays;
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
		return Arrays.stream(roles).filter(role -> role != null).map(IRole::getID).toArray(String[]::new);
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
	public IShard getShard() {
		return getGuild().getShard();
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
		return String.format(DiscordEndpoints.EMOJI_IMAGE, getID());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IEmoji) other).getID().equals(getID());
	}

	@Override
	public String toString() {
		return "<:" + getName() + ":" + getID() + ">";
	}
}
