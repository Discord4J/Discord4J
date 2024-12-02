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
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Member;
import discord4j.core.util.PermissionUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseTopLevelGuildChannel extends BaseChannel implements TopLevelGuildChannel {

    BaseTopLevelGuildChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
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
}