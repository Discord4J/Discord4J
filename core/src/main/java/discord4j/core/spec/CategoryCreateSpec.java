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

import discord4j.common.json.OverwriteEntity;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.Category;
import discord4j.rest.json.request.ChannelCreateRequest;
import reactor.util.annotation.Nullable;

import java.util.Set;

/** A spec used to configure and create a {@link Category}. */
public class CategoryCreateSpec implements AuditSpec<ChannelCreateRequest> {

    private final ChannelCreateRequest.Builder requestBuilder = ChannelCreateRequest.builder()
            .type(Channel.Type.GUILD_CATEGORY.getValue());
    @Nullable
    private String reason;

    /**
     * Sets the name for the created {@link Category}.
     *
     * @param name The name of the category.
     * @return This spec.
     */
    public CategoryCreateSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the position for the created {@link Category}.
     *
     * @param position The raw position for the category.
     * @return This spec.
     */
    public CategoryCreateSpec setPosition(int position) {
        requestBuilder.setPosition(position);
        return this;
    }

    /**
     * Sets the permission overwrites for the created {@link Category}.
     *
     * @param permissionOverwrites The {@code Set<PermissionOverwrite>} which contains overwrites for the category.
     * @return This spec.
     */
    public CategoryCreateSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        OverwriteEntity[] raw = permissionOverwrites.stream()
                .map(o -> new OverwriteEntity(o.getTargetId().asLong(), o.getType().getValue(),
                        o.getAllowed().getRawValue(), o.getDenied().getRawValue()))
                .toArray(OverwriteEntity[]::new);

        requestBuilder.permissionOverwrites(raw);
        return this;
    }

    @Override
    public CategoryCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public ChannelCreateRequest asRequest() {
        return requestBuilder.build();
    }
}
