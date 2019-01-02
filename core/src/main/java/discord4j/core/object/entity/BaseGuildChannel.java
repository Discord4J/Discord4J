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
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.data.stored.GuildChannelBean;
import discord4j.core.object.data.stored.PermissionOverwriteBean;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.PermissionUtil;
import discord4j.rest.json.request.PermissionsEditRequest;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public final Set<ExtendedPermissionOverwrite> getPermissionOverwrites() {
        final PermissionOverwriteBean[] permissionOverwrites = getData().getPermissionOverwrites();
        if (permissionOverwrites == null) {
            return Collections.emptySet();
        } else {
            long guildId = getGuildId().asLong();
            long channelId = getId().asLong();
            return Arrays.stream(permissionOverwrites)
                .map(bean -> new ExtendedPermissionOverwrite(getServiceMediator(), bean, guildId, channelId))
                .collect(Collectors.toSet());
        }
    }

    @Override
    public Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId) {
        return getPermissionOverwrites().stream()
            .filter(overwrite -> overwrite.getMemberId().map(memberId::equals).orElse(false))
            .findFirst();
    }

    @Override
    public Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId) {
        return getPermissionOverwrites().stream()
            .filter(overwrite -> overwrite.getRoleId().map(roleId::equals).orElse(false))
            .findFirst();
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Snowflake memberId) {
        Mono<Member> getMember = getClient().getMemberById(getGuildId(), memberId);
        Mono<PermissionSet> getBasePerms = getMember.flatMap(Member::getBasePermissions);

        return Mono.zip(getMember, getBasePerms, (member, basePerms) -> {
            PermissionOverwrite everyoneOverwrite = getOverwriteForRole(getGuildId()).orElse(null);

            List<PermissionOverwrite> roleOverwrites = member.getRoleIds().stream()
                .map(this::getOverwriteForRole)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)) // jdk 9 Optional#stream
                .collect(Collectors.toList());
            PermissionOverwrite memberOverwrite = getOverwriteForMember(member.getId()).orElse(null);

            return PermissionUtil.computePermissions(basePerms, everyoneOverwrite, roleOverwrites, memberOverwrite);
        });
    }

    @Override
    public final String getName() {
        return getData().getName();
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
    public Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = new PermissionsEditRequest(allow.getRawValue(), deny.getRawValue(), "member");

        return getServiceMediator().getRestClient().getChannelService()
            .editChannelPermissions(getId().asLong(), memberId.asLong(), request);
    }

    @Override
    public Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = new PermissionsEditRequest(allow.getRawValue(), deny.getRawValue(), "role");

        return getServiceMediator().getRestClient().getChannelService()
            .editChannelPermissions(getId().asLong(), roleId.asLong(), request);
    }

    @Override
    GuildChannelBean getData() {
        return (GuildChannelBean) super.getData();
    }

    @Override
    public String toString() {
        return "BaseGuildChannel{} " + super.toString();
    }
}
