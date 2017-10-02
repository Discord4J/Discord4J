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
package sx.blah.discord.handle.impl.events.guild.category;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.ICategory;

/**
 * A generic category-related event.
 */
public abstract class CategoryEvent extends GuildEvent {

	private final ICategory category;

	public CategoryEvent(ICategory category) {
		super(category.getGuild());
		this.category = category;
	}

	/**
	 * Gets the category involved in the event.
	 *
	 * @return The category involved.
	 */
	public ICategory getCategory() {
		return this.category;
	}
}
