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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.event.Update;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.PermissionOverwrite;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class CategoryUpdateEvent extends ChannelEvent {

    private final Category old;
    private final Category current;

    public CategoryUpdateEvent(DiscordClient client, Category old, @Nullable Category current) {
        super(client);
        this.old = old;
        this.current = current;
    }

    public Category getOld() {
        return old;
    }

    public Optional<Category> getCurrent() {
        return Optional.ofNullable(current);
    }
}
