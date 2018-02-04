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
import sx.blah.discord.api.internal.json.objects.EmojiObject;
import sx.blah.discord.api.internal.json.requests.EmojiEditRequest;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.cache.Cache;

import java.util.ArrayList;
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
	 * The name of the emoji.
	 */
	private volatile String name;
	/**
	 * The roles which are allowed to use the emoji.
	 */
	public final Cache<IRole> roles;
	/**
	 * Whether the emoji needs colons in chat.
	 */
	private final boolean requiresColons;
	/**
	 * Whether the emoji is managed by an external service.
	 */
	private final boolean isManaged;

	/**
	 * Whether the emoji is animated.
	 */
	private final boolean isAnimated;

	public EmojiImpl(long id, IGuild guild, String name, boolean requiresColons, boolean isManaged, boolean isAnimated) {
		this(id, guild, name, new Cache<>((DiscordClientImpl) guild.getClient(), IRole.class), requiresColons, isManaged, isAnimated);
	}

	public EmojiImpl(long id, IGuild guild, String name, Cache<IRole> roles, boolean requiresColons, boolean isManaged, boolean isAnimated) {
		this.id = id;
		this.guild = guild;
		this.name = name;
		this.roles = roles;
		this.requiresColons = requiresColons;
		this.isManaged = isManaged;
		this.isAnimated = isAnimated;
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
		return new ArrayList<>(roles.values());
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
		return String.format(DiscordEndpoints.EMOJI_IMAGE + (isAnimated ? ".gif" : ".png"), getStringID());
	}

	@Override
	public void changeRoles(IRole[] roles) {
		PermissionUtils.requirePermissions(getGuild(), getClient().getOurUser(), Permissions.MANAGE_EMOJIS);


		EmojiObject response = ((DiscordClientImpl) getClient()).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.GUILDS + getGuild().getStringID() + "/emojis/" + getStringID(),
				new EmojiEditRequest(getName(), roles),
				EmojiObject.class);
		IEmoji emoji = DiscordUtils.getEmojiFromJSON(getGuild(), response);
	}

	@Override
	public void changeName(String name) {
		PermissionUtils.requirePermissions(getGuild(), getClient().getOurUser(), Permissions.MANAGE_EMOJIS);

		EmojiObject response = ((DiscordClientImpl) getClient()).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.GUILDS + getGuild().getStringID() + "/emojis/" + getStringID(),
				new EmojiEditRequest(name, getRoles().toArray(new IRole[getRoles().size()])),
				EmojiObject.class);
		IEmoji emoji = DiscordUtils.getEmojiFromJSON(getGuild(), response);
	}

	@Override
	public void deleteEmoji() {
		PermissionUtils.requirePermissions(getGuild(), getClient().getOurUser(), Permissions.MANAGE_EMOJIS);

		((DiscordClientImpl) getClient()).REQUESTS.DELETE.makeRequest(
				DiscordEndpoints.GUILDS + getGuild().getStringID() + "/emojis/" + getStringID());

		((Guild) guild).emojis.remove(this);
	}

	/**
	 * Sets the CACHED name of the emoji.
	 *
	 * @param name The name of the emoji.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the CACHED roles of the emoji.
	 *
	 * @param roles The roles of the emoji.
	 */
	public void setRoles(List<IRole> roles) {
		this.roles.clear();
		this.roles.putAll(roles);
	}

	@Override
	public boolean isDeleted() {
		return !this.equals(getGuild().getEmojiByID(getLongID()));
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
	public boolean isAnimated() {
		return isAnimated;
	}

	@Override
	public IEmoji copy() {
		return new EmojiImpl(id, guild, name, roles, requiresColons, isManaged, isAnimated);
	}

	@Override
	public String toString() {
		return "<" + (isAnimated ? "a" : "") + ":" + getName() + ":" + getStringID() + ">";
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
