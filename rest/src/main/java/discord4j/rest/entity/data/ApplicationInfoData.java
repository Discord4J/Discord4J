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

import discord4j.rest.json.response.ApplicationInfoResponse;
import reactor.util.annotation.Nullable;

public class ApplicationInfoData {

    private final long id;
    private final String name;
    @Nullable
    private final String icon;
    @Nullable
    private final String description;
    private final boolean botPublic;
    private final boolean botRequireCodeGrant;
    private final long ownerId;

    public ApplicationInfoData(ApplicationInfoResponse response) {
        id = response.getId();
        name = response.getName();
        icon = response.getIcon();
        description = response.getDescription();
        botPublic = response.isBotPublic();
        botRequireCodeGrant = response.isBotRequireCodeGrant();
        ownerId = response.getOwner().getId();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isBotPublic() {
        return botPublic;
    }

    public boolean isBotRequireCodeGrant() {
        return botRequireCodeGrant;
    }

    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "ApplicationInfoData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", description='" + description + '\'' +
                ", botPublic=" + botPublic +
                ", botRequireCodeGrant=" + botRequireCodeGrant +
                ", ownerId=" + ownerId +
                '}';
    }
}
