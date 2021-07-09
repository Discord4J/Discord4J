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
package discord4j.core.spec.legacy;

import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ImmutableRoleModifyRequest;
import discord4j.discordjson.json.RoleModifyRequest;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import reactor.util.annotation.Nullable;


/**
 * LegacySpec used to modify a guild {@link Role}.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-role">Modify Guild Role</a>
 */
public class LegacyRoleEditSpec implements LegacyAuditSpec<RoleModifyRequest> {

    private final ImmutableRoleModifyRequest.Builder requestBuilder = RoleModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the new name of the {@link Role}.
     *
     * @param name The role name.
     * @return This spec.
     */
    public LegacyRoleEditSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the new permissions for the {@link Role}.
     *
     * @param permissions The role permissions.
     * @return This spec.
     */
    public LegacyRoleEditSpec setPermissions(PermissionSet permissions) {
        requestBuilder.permissions(permissions.getRawValue());
        return this;
    }

    /**
     * Sets the new color of the {@link Role}.
     *
     * @param color The role color.
     * @return This spec.
     */
    public LegacyRoleEditSpec setColor(Color color) {
        requestBuilder.color(color.getRGB());
        return this;
    }

    /**
     * Sets whether the modified {@link Role} should be displayed separately in the sidebar.
     *
     * @param hoist The role hoisted property.
     * @return This spec.
     */
    public LegacyRoleEditSpec setHoist(boolean hoist) {
        requestBuilder.hoist(hoist);
        return this;
    }

    /**
     * Sets whether the modified {@link Role} should be mentionable.
     *
     * @param mentionable The role mentionable property.
     * @return This spec.
     */
    public LegacyRoleEditSpec setMentionable(boolean mentionable) {
        requestBuilder.mentionable(mentionable);
        return this;
    }

    @Override
    public LegacyRoleEditSpec setReason(@Nullable final String reason) {
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
