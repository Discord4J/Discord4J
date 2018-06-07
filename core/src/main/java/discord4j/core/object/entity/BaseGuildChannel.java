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
package discord4j.core.object.entity;

import discord4j.core.ServiceMediator;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.data.stored.GuildChannelBean;
import discord4j.core.object.data.stored.PermissionOverwriteBean;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** An internal implementation of {@link GuildChannel} designed to streamline inheritance. */
class BaseGuildChannel extends BaseChannel implements GuildChannel {

    /**
     * Constructs an {@code BaseGuildChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseGuildChannel(final ServiceMediator serviceMediator, final GuildChannelBean data) {
        super(serviceMediator, data);
    }

    @Override
    public final Snowflake getGuildId() {
        return Snowflake.of(getData().getGuildId());
    }

    @Override
    public final Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public final Set<PermissionOverwrite> getPermissionOverwrites() {
        final PermissionOverwriteBean[] permissionOverwrites = getData().getPermissionOverwrites();
        return (permissionOverwrites == null) ? Collections.emptySet() : Arrays.stream(permissionOverwrites)
                .map(bean ->
                        new PermissionOverwrite(getServiceMediator(), bean, getGuildId().asLong(), getId().asLong()))
                .collect(Collectors.toSet());
    }
    
    @Override
    public Mono<PermissionSet> getPermissions(Member member) {
        Set<PermissionOverwrite> effectiveOverwrites = getPermissionOverwrites().stream()
                .filter(overwrite -> overwrite.getRoleId().map(member.getRoleIds()::contains).orElse(true))
                .filter(overwrite -> overwrite.getUserId().map(member.getId()::equals).orElse(true))
                .collect(Collectors.toSet());
                
        PermissionSet granted = effectiveOverwrites.stream().map(PermissionOverwrite::getAllowed)
                .reduce(PermissionSet.none(), PermissionSet::or);
        
        PermissionSet revoked = effectiveOverwrites.stream().map(PermissionOverwrite::getDenied)
                .reduce(PermissionSet.none(), PermissionSet::or);
        
        return member.getPermissions()
                .map(perms -> perms.or(granted))
                .map(perms -> perms.and(revoked.inverse()));
    }

    @Override
    public final String getName() {
        return getData().getName();
    }

    @Override
    public final Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getParentId()).map(Snowflake::of);
    }

    @Override
    public final Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getCategoryById);
    }

    @Override
    public int getRawPosition() {
        return getData().getPosition();
    }

    @Override
    public final Mono<Integer> getPosition() {
        return getGuild().flatMapMany(Guild::getChannels).collectList().map(list -> list.indexOf(this));
    }

    @Override
    protected GuildChannelBean getData() {
        return (GuildChannelBean) super.getData();
    }
}
