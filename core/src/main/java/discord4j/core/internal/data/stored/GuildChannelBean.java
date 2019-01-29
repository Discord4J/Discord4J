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
package discord4j.core.internal.data.stored;

import discord4j.common.json.OverwriteEntity;
import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.rest.json.response.ChannelResponse;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class GuildChannelBean extends ChannelBean {

    private static final long serialVersionUID = -3061996202860792100L;

    private long guildId;
    @Nullable
    private PermissionOverwriteBean[] permissionOverwrites;
    private String name;
    @Nullable
    private Long parentId;
    private int position;

    public GuildChannelBean(final GatewayChannelResponse channel, final long guildId) {
        super(channel.getId(), channel.getType());
        this.guildId = guildId;

        final OverwriteEntity[] overwrites = channel.getPermissionOverwrites();
        permissionOverwrites = Arrays.stream(overwrites)
                .map(PermissionOverwriteBean::new)
                .toArray(PermissionOverwriteBean[]::new);

        name = channel.getName();
        parentId = channel.getParentId();
        position = channel.getPosition();
    }

    public GuildChannelBean(final ChannelResponse response) {
        super(response);

        guildId = Objects.requireNonNull(response.getGuildId());
        final OverwriteEntity[] overwrites = response.getPermissionOverwrites();
        permissionOverwrites = overwrites == null ? null : Arrays.stream(overwrites)
                .map(PermissionOverwriteBean::new)
                .toArray(PermissionOverwriteBean[]::new);

        name = Objects.requireNonNull(response.getName());
        parentId = response.getParentId();
        position = Objects.requireNonNull(response.getPosition());
    }

    public GuildChannelBean() {}

    public final long getGuildId() {
        return guildId;
    }

    public final void setGuildId(final Long guildId) {
        this.guildId = guildId;
    }

    @Nullable
    public final PermissionOverwriteBean[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    public final void setPermissionOverwrites(@Nullable final PermissionOverwriteBean[] permissionOverwrites) {
        this.permissionOverwrites = permissionOverwrites;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    @Nullable
    public final Long getParentId() {
        return parentId;
    }

    public final void setParentId(@Nullable final Long parentId) {
        this.parentId = parentId;
    }

    public final int getPosition() {
        return position;
    }

    public final void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "GuildChannelBean{" +
                "guildId=" + guildId +
                ", permissionOverwrites=" + Arrays.toString(permissionOverwrites) +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", position=" + position +
                '}';
    }
}
