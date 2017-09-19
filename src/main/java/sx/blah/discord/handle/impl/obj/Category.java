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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;
import java.util.stream.Collectors;

public class Category implements ICategory {

	private final IDiscordClient client;
	private final IShard shard;
	private volatile String name;
	private final long id;
	private volatile IGuild guild;
	private volatile int position;
	private volatile boolean nsfw;

	public Category(IShard shard, String name, long id, IGuild guild, int position, boolean nsfw) {
		this.shard = shard;
		this.client = shard.getClient();
		this.name = name;
		this.guild = guild;
		this.position = position;
		this.id = id;
		this.nsfw = nsfw;
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
	public String getName() {
		return name;
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
		return new Category(shard, name, id, guild, position, nsfw);
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
	public int hashCode() {
		return Long.hashCode(getLongID());
	}

	@Override
	public boolean equals(Object obj) {
		return DiscordUtils.equals(this, obj);
	}
}
