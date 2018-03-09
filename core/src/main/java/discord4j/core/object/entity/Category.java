/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.core.Client;
import discord4j.core.object.entity.bean.CategoryBean;
import reactor.core.publisher.Flux;

/** A Discord category. */
public final class Category extends BaseGuildChannel {

    /**
     * Constructs an {@code Category} with an associated client and Discord data.
     *
     * @param client The Client associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Category(final Client client, final CategoryBean data) {
        super(client, data);
    }

    /**
     * Requests to retrieve the channels residing in this category.
     *
     * @return A {@link Flux} that continually emits the {@link GuildChannel channels} residing in this category. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<GuildChannel> getChannels() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }
}
