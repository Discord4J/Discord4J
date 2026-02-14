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
import discord4j.discordjson.json.RoleCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import org.jspecify.annotations.Nullable;


/**
 * LegacySpec used to create a new guild {@link Role} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-role">Create Guild Role</a>
 */
public class LegacyRoleCreateSpec implements LegacyAuditSpec<RoleCreateRequest> {

    private @Nullable String name;
    private @Nullable Long permissions;
    private @Nullable Integer color;
    private @Nullable Boolean hoist;
    private @Nullable Boolean mentionable;
    private @Nullable String reason;

    /**
     * Sets the name of the created {@link Role}.
     *
     * @param name The role name.
     * @return This spec.
     */
    public LegacyRoleCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the enabled/disabled permissions, in the form of a {@link PermissionSet} for the created {@link Role}.
     *
     * @param permissions The role permissions.
     * @return This spec.
     */
    public LegacyRoleCreateSpec setPermissions(PermissionSet permissions) {
        this.permissions = permissions.getRawValue();
        return this;
    }

    /**
     * Sets the color of the created {@link Role}.
     *
     * @param color The role color.
     * @return This spec.
     */
    public LegacyRoleCreateSpec setColor(Color color) {
        this.color = color.getRGB();
        return this;
    }

    /**
     * Sets whether the created {@link Role} should be displayed separately in the sidebar.
     *
     * @param hoist The role hoisted property.
     * @return This spec.
     */
    public LegacyRoleCreateSpec setHoist(boolean hoist) {
        this.hoist = hoist;
        return this;
    }

    /**
     * Sets whether the created {@link Role} should be mentionable.
     *
     * @param mentionable The role mentionable property.
     * @return This spec.
     */
    public LegacyRoleCreateSpec setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
        return this;
    }

    @Override
    public LegacyRoleCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public RoleCreateRequest asRequest() {
        return RoleCreateRequest.builder()
                .name(name == null ? Possible.absent() : Possible.of(name))
                .permissions(permissions == null ? Possible.absent() : Possible.of(permissions))
                .color(color == null ? Possible.absent() : Possible.of(color))
                .hoist(hoist == null ? Possible.absent() : Possible.of(hoist))
                .mentionable(mentionable == null ? Possible.absent() : Possible.of(mentionable))
                .build();
    }
}
