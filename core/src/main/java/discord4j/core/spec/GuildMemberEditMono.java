package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.GuildMemberModifyRequest;
import discord4j.discordjson.json.ImmutableGuildMemberModifyRequest;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class GuildMemberEditMono extends AuditableRequest<MemberData, ImmutableGuildMemberModifyRequest.Builder, GuildMemberEditMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final long memberId;

    public GuildMemberEditMono(Supplier<ImmutableGuildMemberModifyRequest.Builder> requestBuilder,
                               @Nullable  String reason, GatewayDiscordClient gateway, long guildId, long memberId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
        this.memberId = memberId;
    }

    public GuildMemberEditMono(GatewayDiscordClient gateway, long guildId, long memberId) {
        this(GuildMemberModifyRequest::builder, null, gateway, guildId, memberId);
    }

    @Override
    GuildMemberEditMono withBuilder(UnaryOperator<ImmutableGuildMemberModifyRequest.Builder> f) {
        return new GuildMemberEditMono(apply(f), reason, gateway, guildId, memberId);
    }

    public GuildMemberEditMono withNewVoiceChannel(@Nullable Snowflake channel) {
        Optional<String> value = Optional.ofNullable(channel).map(Snowflake::asString);
        return withBuilder(it -> it.channelId(Possible.of(value)));
    }

    public GuildMemberEditMono withMute(boolean mute) {
        return withBuilder(it -> it.mute(mute));
    }

    public GuildMemberEditMono withDeafen(boolean deaf) {
        return withBuilder(it -> it.deaf(deaf));
    }

    public GuildMemberEditMono withNickname(@Nullable String nickname) {
        // TODO: need to worry about https://github.com/Discord4J/Discord4J/commit/6ed922a36ea1f1507d209db1203a347aa1652b60#diff-5e204723a09b563d65a5c14ebffeba6a31c8f6b378206409e57419d9156c7904 ?
        Optional<String> value = Optional.ofNullable(nickname);
        return withBuilder(it -> it.nick(Possible.of(value)));
    }

    public GuildMemberEditMono withRoles(Set<Snowflake> roles) {
        List<String> list = roles.stream().map(Snowflake::asString).collect(Collectors.toList());
        return withBuilder(it -> it.roles(list));
    }

    @Override
    public GuildMemberEditMono withReason(String reason) {
        return new GuildMemberEditMono(requestBuilder, reason, gateway, guildId, memberId);
    }

    @Override
    Mono<MemberData> getRequest() {
        return gateway.getRestClient().getGuildService()
                .modifyGuildMember(guildId, memberId, requestBuilder.get().build(), reason);
    }
}
