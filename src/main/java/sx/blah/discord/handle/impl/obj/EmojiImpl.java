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
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.List;
import java.util.Objects;

/**
 * The default implementation of {@link IEmoji}.
 */
public class EmojiImpl implements IEmoji {

	/**
	 * The unique snowflake ID of the emoji.
	 */
	private final long id;
	/**
	 * The parent guild of the emoji.
	 */
	private final IGuild guild;
	/**
	 * the name of the emoji.
	 */
	private final String name;
	/**
	 * The roles which are allowed to use the emoji.
	 */
	private final List<IRole> roles;
	/**
	 * Whether the emoji needs colons in chat.
	 */
	private final boolean requiresColons;
	/**
	 * Whether the emoji is managed by an external service.
	 */
	private final boolean isManaged;

	public EmojiImpl(long id, IGuild guild, String name, List<IRole> roles, boolean requiresColons, boolean isManaged) {
		this.id = id;
		this.guild = guild;
		this.name = name;
		this.roles = roles;
		this.requiresColons = requiresColons;
		this.isManaged = isManaged;
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<IRole> getRoles() {
		return roles;
	}

	@Override
	public boolean requiresColons() {
		return requiresColons;
	}

	@Override
	public boolean isManaged() {
		return isManaged;
	}

	@Override
	public String getImageUrl() {
		return String.format(DiscordEndpoints.EMOJI_IMAGE, getStringID());
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
		return new EmojiImpl(id, guild, name, roles, requiresColons, isManaged);
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
