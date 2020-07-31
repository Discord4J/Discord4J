package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class BanMono extends AuditableRequest<Void, Void, BanMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;
    private final long memberId;

    @Nullable
    private final Integer deleteMessageDays;

    public BanMono(GatewayDiscordClient gateway, long guildId, long memberId, @Nullable Integer deleteMessageDays,
                   @Nullable String reason) {
        super(() -> { throw new UnsupportedOperationException("BanMono has no internal builder."); }, reason);
        this.gateway = gateway;
        this.guildId = guildId;
        this.memberId = memberId;
        this.deleteMessageDays = deleteMessageDays;
    }

    public BanMono(GatewayDiscordClient gateway, long guildId, long memberId) {
        this(gateway, guildId, memberId, null, null);
    }

    @Override
    BanMono withBuilder(UnaryOperator<Void> f) {
        throw new UnsupportedOperationException("BanMono has no internal builder.");
    }

    public BanMono withDeleteMessageDays(final int deleteMessageDays) {
        return new BanMono(gateway, guildId, memberId, deleteMessageDays, reason);
    }

    @Override
    public BanMono withReason(final String reason) {
        return new BanMono(gateway, guildId, memberId, deleteMessageDays, reason);
    }

    @Override
    public Mono<Void> getRequest() {
        Map<String, Object> queryParams = new HashMap<>(2);
        if (deleteMessageDays != null) {
            queryParams.put("delete_message_days", deleteMessageDays);
        }
        if (reason != null) {
            queryParams.put("reason", reason);
        }

        return gateway.getRestClient().getGuildService()
                .createGuildBan(guildId, memberId, queryParams, reason);
    }
}
