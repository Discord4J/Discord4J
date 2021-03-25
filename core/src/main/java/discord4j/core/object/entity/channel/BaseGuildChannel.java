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
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.util.OrderUtil;
import discord4j.core.util.PermissionUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.PermissionsEditRequest;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** An internal implementation of {@link GuildChannel} designed to streamline inheritance. */
class BaseGuildChannel extends BaseChannel implements GuildChannel {

    /**
     * Constructs an {@code BaseGuildChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseGuildChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    @Override
    public final Snowflake getGuildId() {
        return getData().guildId().toOptional()
            .map(Snowflake::of)
            .orElseThrow(IllegalStateException::new); // TODO
    }

    @Override
    public final Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    @Override
    public final Set<ExtendedPermissionOverwrite> getPermissionOverwrites() {
        return getData().permissionOverwrites().toOptional()
            .map(permissionOverwrites -> {
                long guildId = getGuildId().asLong();
                long channelId = getId().asLong();
                return permissionOverwrites.stream()
                    .map(overwriteData -> new ExtendedPermissionOverwrite(getClient(), overwriteData, guildId, channelId))
                    .collect(Collectors.toSet());
            })
            .orElse(Collections.emptySet());
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
        return getClient().getMemberById(getGuildId(), memberId)
            .flatMap(this::getEffectivePermissions);
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Member member) {
        return member.getBasePermissions().map(basePerms -> {
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
        return getData().name().toOptional()
            .orElseThrow(IllegalStateException::new);
    }

    @Override
    public int getRawPosition() {
        return getData().position().toOptional()
            .orElseThrow(IllegalStateException::new);
    }

    @Override
    public final Mono<Integer> getPosition() {
        return getGuild()
                .flatMapMany(Guild::getChannels)
                .transform(OrderUtil::orderGuildChannels)
                .collectList()
                .map(channels -> channels.indexOf(this));
    }

    @Override
    public Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite, @Nullable String reason) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = PermissionsEditRequest.builder()
                .allow(allow.getRawValue())
                .deny(deny.getRawValue())
                .type(PermissionOverwrite.Type.MEMBER.getValue())
                .build();

        return getClient().getRestClient().getChannelService()
                .editChannelPermissions(getId().asLong(), memberId.asLong(), request, reason);
    }

    @Override
    public Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite, @Nullable String reason) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = PermissionsEditRequest.builder()
                .allow(allow.getRawValue())
                .deny(deny.getRawValue())
                .type(PermissionOverwrite.Type.ROLE.getValue())
                .build();

        return getClient().getRestClient().getChannelService()
                .editChannelPermissions(getId().asLong(), roleId.asLong(), request, reason);
    }

    @Override
    public String toString() {
        return "BaseGuildChannel{} " + super.toString();
    }
}
