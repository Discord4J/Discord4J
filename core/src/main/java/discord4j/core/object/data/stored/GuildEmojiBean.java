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
package discord4j.core.object.data.stored;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.common.json.GuildEmojiResponse;

import java.io.Serializable;
import java.util.Arrays;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class GuildEmojiBean implements Serializable {

    private static final long serialVersionUID = -514669448967485261L;

    private long id;
    private String name;
    private long[] roles;
    private boolean requireColons;
    private boolean managed;
    private boolean animated;
    private boolean available;

    public GuildEmojiBean(final GuildEmojiResponse response) {
        id = response.getId();
        name = response.getName();
        roles = response.getRoles();
        requireColons = response.isRequireColons();
        managed = response.isManaged();
        animated = response.isAnimated();
        available = response.isAvailable();
    }

    public GuildEmojiBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long[] getRoles() {
        return roles;
    }

    public void setRoles(final long[] roles) {
        this.roles = roles;
    }

    public boolean isRequireColons() {
        return requireColons;
    }

    public void setRequireColons(final boolean requireColons) {
        this.requireColons = requireColons;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(final boolean managed) {
        this.managed = managed;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(final boolean animated) {
        this.animated = animated;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "GuildEmojiBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", requireColons=" + requireColons +
                ", managed=" + managed +
                ", animated=" + animated +
                '}';
    }
}
