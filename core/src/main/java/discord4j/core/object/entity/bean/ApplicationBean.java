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

import discord4j.common.json.response.ApplicationInfoResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class ApplicationBean implements Serializable {

    private static final long serialVersionUID = 2418495859931703844L;

    private long id;
    private String name;
    @Nullable
    private String icon;
    @Nullable
    private String description;
    private boolean botPublic;
    private boolean botRequireCodeGrant;
    private long ownerId;

    public ApplicationBean(final ApplicationInfoResponse response) {
        id = response.getId();
        name = response.getName();
        icon = response.getIcon();
        description = response.getDescription();
        botPublic = response.isBotPublic();
        botRequireCodeGrant = response.isBotRequireCodeGrant();
        ownerId = response.getOwner().getId();
    }

    public ApplicationBean() {}

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
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable final String icon) {
        this.icon = icon;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description;
    }

    public boolean isBotPublic() {
        return botPublic;
    }

    public void setBotPublic(final boolean botPublic) {
        this.botPublic = botPublic;
    }

    public boolean isBotRequireCodeGrant() {
        return botRequireCodeGrant;
    }

    public void setBotRequireCodeGrant(final boolean botRequireCodeGrant) {
        this.botRequireCodeGrant = botRequireCodeGrant;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
    }
}
