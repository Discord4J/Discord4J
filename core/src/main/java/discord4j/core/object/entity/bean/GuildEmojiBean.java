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
package discord4j.core.object.entity.bean;

import discord4j.common.json.response.EmojiResponse;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public final class GuildEmojiBean implements Serializable {

    private static final long serialVersionUID = -514669448967485261L;

    private long id;
    private String name;
    @Nullable
    private long[] roles;
    private long user;
    private boolean requireColons;
    private boolean managed;
    private boolean animated;

    public GuildEmojiBean(final EmojiResponse response) {
        id = Objects.requireNonNull(response.getId());
        name = response.getName();
        roles = Objects.requireNonNull(response.getRoles());
        user = Objects.requireNonNull(response.getUser()).getId();
        requireColons = Objects.requireNonNull(response.isRequireColons());
        managed = Objects.requireNonNull(response.isManaged());
        animated = Objects.requireNonNull(response.isAnimated());
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

    @Nullable
    public long[] getRoles() {
        return roles;
    }

    public void setRoles(@Nullable final long[] roles) {
        this.roles = roles;
    }

    public long getUser() {
        return user;
    }

    public void setUser(final long user) {
        this.user = user;
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
}
