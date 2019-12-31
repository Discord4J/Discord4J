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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.entity.data;

import discord4j.common.json.GuildEmojiResponse;

import java.util.Arrays;

public class GuildEmojiData {

    private final long id;
    private final String name;
    private final long[] roles;
    private final boolean requireColons;
    private final boolean managed;
    private final boolean animated;

    public GuildEmojiData(GuildEmojiResponse response) {
        id = response.getId();
        name = response.getName();
        roles = response.getRoles();
        requireColons = response.isRequireColons();
        managed = response.isManaged();
        animated = response.isAnimated();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long[] getRoles() {
        return roles;
    }

    public boolean isRequireColons() {
        return requireColons;
    }

    public boolean isManaged() {
        return managed;
    }

    public boolean isAnimated() {
        return animated;
    }

    @Override
    public String toString() {
        return "GuildEmojiData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", requireColons=" + requireColons +
                ", managed=" + managed +
                ", animated=" + animated +
                '}';
    }
}
