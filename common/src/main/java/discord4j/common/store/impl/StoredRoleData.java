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

package discord4j.common.store.impl;

import discord4j.discordjson.json.RoleData;

import static discord4j.common.store.impl.ImplUtils.toLongId;

class StoredRoleData {

    private final long id;
    private final String name;
    private final int color;
    private final boolean hoist;
    private final int position;
    private final long permissions;
    private final boolean managed;
    private final boolean mentionable;

    StoredRoleData(RoleData original) {
        this.id = toLongId(original.id());
        this.name = original.name();
        this.color = original.color();
        this.hoist = original.hoist();
        this.position = original.position();
        this.permissions = original.permissions();
        this.managed = original.managed();
        this.mentionable = original.mentionable();
    }

    RoleData toImmutable() {
        return RoleData.builder()
                .id("" + id)
                .name(name)
                .color(color)
                .hoist(hoist)
                .position(position)
                .permissions(permissions)
                .managed(managed)
                .mentionable(mentionable)
                .build();
    }
}
