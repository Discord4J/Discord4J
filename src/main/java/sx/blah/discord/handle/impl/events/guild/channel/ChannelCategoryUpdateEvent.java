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
package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Dispatched when a channel is moved in or out of a category.
 */
public class ChannelCategoryUpdateEvent extends ChannelUpdateEvent {

	private final ICategory oldCategory;
	private final ICategory newCategory;

	public ChannelCategoryUpdateEvent(IChannel oldChannel, IChannel newChannel,
									  ICategory oldCategory, ICategory newCategory) {
		super(oldChannel, newChannel);
		this.oldCategory = oldCategory;
		this.newCategory = newCategory;
	}

	/**
	 * Returns the category that the channel now resides in.
	 *
	 * @return The category that the channel now resides in, may be null.
	 */
	public ICategory getNewCategory() {
		return this.newCategory;
	}

	/**
	 * Returns the category that the channel used to reside in.
	 *
	 * @return The category that the channel used  to reside in, may be null.
	 */
	public ICategory getOldCategory() {
		return this.oldCategory;
	}
}
