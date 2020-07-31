package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.GuildModifyRequest;
import discord4j.discordjson.json.ImmutableGuildModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class GuildEditMono extends AuditableRequest<Guild, ImmutableGuildModifyRequest.Builder, GuildEditMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final GuildData data;

    public GuildEditMono(Supplier<ImmutableGuildModifyRequest.Builder> requestBuilder, @Nullable String reason,
                         GatewayDiscordClient gateway, long guildId, GuildData data) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
        this.data = data;
    }

    public GuildEditMono(GatewayDiscordClient gateway, long guildId, GuildData data) {
        this(GuildModifyRequest::builder, null, gateway, guildId, data);
    }

    @Override
    GuildEditMono withBuilder(UnaryOperator<ImmutableGuildModifyRequest.Builder> f) {
        return new GuildEditMono(apply(f), reason, gateway, guildId, data);
    }

    public GuildEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    public GuildEditMono withRegion(Region region) {
        return withBuilder(it -> it.region(region.getId()));
    }

    public GuildEditMono withVerificationLevel(Guild.VerificationLevel verificationLevel) {
        return withBuilder(it -> it.verificationLevel(verificationLevel.getValue()));
    }

    public GuildEditMono withDefaultMessageNotificationsLevel(Guild.NotificationLevel notificationsLevel) {
        return withBuilder(it -> it.defaultMessageNotifications(notificationsLevel.getValue()));
    }

    public GuildEditMono withAfkChanelId(@Nullable Snowflake afkChanelId) {
        Optional<String> value = Optional.ofNullable(afkChanelId).map(Snowflake::asString);
        return withBuilder(it -> it.afkChannelId(Possible.of(value)));
    }

    public GuildEditMono withAfkTimeout(int afkTimeout) {
        return withBuilder(it -> it.afkTimeout(afkTimeout));
    }

    public GuildEditMono withIcon(@Nullable Image icon) {
        Optional<String> value = Optional.ofNullable(icon).map(Image::getDataUri);
        return withBuilder(it -> it.icon(Possible.of(value)));
    }

    public GuildEditMono withOwnerId(Snowflake ownerId) {
        return withBuilder(it -> it.ownerId(ownerId.asString()));
    }

    public GuildEditMono withBanner(@Nullable Image banner) {
        Optional<String> value = Optional.ofNullable(banner).map(Image::getDataUri);
        return withBuilder(it -> it.banner(Possible.of(value)));
    }

    public GuildEditMono withContentFilterLevel(Guild.ContentFilterLevel contentFilterLevel) {
        return withBuilder(it -> it.explicitContentFilter(contentFilterLevel.getValue()));
    }

    @Override
    public GuildEditMono withReason(String reason) {
        return new GuildEditMono(requestBuilder, reason, gateway, guildId, data);
    }

    @Override
    Mono<Guild> getRequest() {
        return gateway.getRestClient().getGuildService()
                .modifyGuild(guildId, requestBuilder.get().build(), reason)
                .map(data -> new Guild(gateway, GuildData.builder()
                        .from(this.data)
                        .from(data)
                        .build()));
    }
}
