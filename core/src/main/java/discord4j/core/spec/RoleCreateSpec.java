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
package discord4j.core.spec;

import discord4j.core.object.util.PermissionSet;
import discord4j.rest.json.request.RoleCreateRequest;

import java.awt.*;

public class RoleCreateSpec implements Spec<RoleCreateRequest> {

    private String name;
    private long permissions;
    private int color;
    private boolean hoist;
    private boolean mentionable;

    public RoleCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    public RoleCreateSpec setPermissions(PermissionSet permissions) {
        this.permissions = permissions.getRawValue();
        return this;
    }

    public RoleCreateSpec setColor(Color color) {
        this.color = color.getRGB() & 0xFFFFFF;
        return this;
    }

    public RoleCreateSpec setHoist(boolean hoist) {
        this.hoist = hoist;
        return this;
    }

    public RoleCreateSpec setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
        return this;
    }

    @Override
    public RoleCreateRequest asRequest() {
        return new RoleCreateRequest(name, permissions, color, hoist, mentionable);
    }
}
