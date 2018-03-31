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
import discord4j.core.object.PermissionOverwrite;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class CategoryUpdateEvent extends ChannelEvent {

    private final Update<String> name;
    private final Update<Set<PermissionOverwrite>> overwrites;
    private final Update<Integer> position;

    public CategoryUpdateEvent(DiscordClient client, @Nullable Update<String> name,
                               @Nullable Update<Set<PermissionOverwrite>> overwrites,
                               @Nullable Update<Integer> position) {
        super(client);
        this.name = name;
        this.overwrites = overwrites;
        this.position = position;
    }

    public Optional<Update<String>> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<Update<Set<PermissionOverwrite>>> getOverwrites() {
        return Optional.ofNullable(overwrites);
    }

    public Optional<Update<Integer>> getPosition() {
        return Optional.ofNullable(position);
    }
}
