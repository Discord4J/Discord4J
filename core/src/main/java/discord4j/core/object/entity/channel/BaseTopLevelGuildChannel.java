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

class BaseTopLevelGuildChannel extends BaseChannel implements TopLevelGuildChannel {

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
