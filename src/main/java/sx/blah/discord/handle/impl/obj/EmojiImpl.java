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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.cache.Cache;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EmojiImpl implements IEmoji {

	/**
	 * The guild.
	 */
	protected final IGuild guild;
	/**
	 * The ID.
	 */
	protected final long id;
	/**
	 * Roles for integration?
	 */
	protected final Cache<IRole> roles;
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

	public EmojiImpl(IGuild guild, long id, String name, boolean requiresColons, boolean isManaged, String[] roles) {
		this(guild, id, name, requiresColons, isManaged, convertStringsToLongs(roles));
	}

	public EmojiImpl(IGuild guild, long id, String name, boolean requiresColons, boolean isManaged, IRole[] roles) {
		this(guild, id, name, requiresColons, isManaged, convertRolesToIDs(roles));
	}

	public EmojiImpl(IGuild guild, long id, String name, boolean requiresColons, boolean isManaged, long[] roleIds) {
		this.guild = guild;
		this.id = id;
		this.name = name;
		this.requiresColons = requiresColons;
		this.isManaged = isManaged;
		this.roles = new Cache<>((DiscordClientImpl) guild.getClient(), IRole.class);

		for (long roleId : roleIds) {
			IRole role = guild.getRoleByID(roleId);

			if (role != null) {
				this.roles.put(role);
			}
		}
	}

	private static long[] convertRolesToIDs(IRole[] roles) {
		return Arrays.stream(roles).filter(role -> role != null).mapToLong(IRole::getLongID).toArray();
	}

	private static long[] convertStringsToLongs(String[] roles) {
		return Arrays.stream(roles).mapToLong(Long::parseUnsignedLong).toArray();
	}

	public void setRequiresColons(boolean requiresColons) {
		this.requiresColons = requiresColons;
	}

	@Override
	public long getLongID() {
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
		return new EmojiImpl(guild, id, name, requiresColons, isManaged, roles.values().toArray(new IRole[roles.size()]));
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
		return new LinkedList<>(roles.values());
	}

	@Override
	public String getImageUrl() {
		return String.format(DiscordEndpoints.EMOJI_IMAGE, getStringID());
	}

	@Override
	public String toString() {
		return "<:" + getName() + ":" + getStringID() + ">";
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
