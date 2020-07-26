package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class BanMono extends Mono<Void> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final long memberId;

    @Nullable
    private final Integer deleteMessageDays;
    @Nullable
    private final String reason;

    public BanMono(GatewayDiscordClient gateway, long guildId, long memberId, @Nullable Integer deleteMessageDays,
                   @Nullable String reason) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.memberId = memberId;
        this.deleteMessageDays = deleteMessageDays;
        this.reason = reason;
    }

    public BanMono(GatewayDiscordClient gateway, long guildId, long memberId) {
        this(gateway, guildId, memberId, null, null);
    }

    public BanMono withDeleteMessageDays(final int deleteMessageDays) {
        return new BanMono(gateway, guildId, memberId, deleteMessageDays, reason);
    }

    public BanMono withReason(final String reason) {
        return new BanMono(gateway, guildId, memberId, deleteMessageDays, reason);
    }

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        Map<String, Object> queryParams = new HashMap<>(2);
        if (deleteMessageDays != null) {
            queryParams.put("delete_message_days", deleteMessageDays);
        }
        if (reason != null) {
            queryParams.put("reason", reason);
        }

        gateway.getRestClient().getGuildService()
                .createGuildBan(guildId, memberId, queryParams, reason)
                .subscribe(actual);
    }
}
