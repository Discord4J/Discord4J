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
import discord4j.rest.json.request.RoleModifyRequest;
import reactor.util.annotation.Nullable;

import java.awt.Color;

public class RoleEditSpec implements AuditSpec<RoleModifyRequest> {

    private final RoleModifyRequest.Builder requestBuilder = RoleModifyRequest.builder();
    @Nullable
    private String reason;

    public RoleEditSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    public RoleEditSpec setPermissions(PermissionSet permissions) {
        requestBuilder.permissions(permissions.getRawValue());
        return this;
    }

    public RoleEditSpec setColor(Color color) {
        requestBuilder.color(color.getRGB() & 0xFFFFFF);
        return this;
    }

    public RoleEditSpec setHoist(boolean hoist) {
        requestBuilder.hoist(hoist);
        return this;
    }

    public RoleEditSpec setMentionable(boolean mentionable) {
        requestBuilder.mentionable(mentionable);
        return this;
    }

    @Override
    public RoleEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public RoleModifyRequest asRequest() {
        return requestBuilder.build();
    }
}
