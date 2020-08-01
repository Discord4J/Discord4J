package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ExtendedInvite;
import discord4j.discordjson.json.ImmutableInviteCreateRequest;
import discord4j.discordjson.json.InviteCreateRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class InviteCreateMono extends AuditableRequest<ExtendedInvite, ImmutableInviteCreateRequest.Builder, InviteCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long channelId;

    public InviteCreateMono(Supplier<ImmutableInviteCreateRequest.Builder> requestBuilder, @Nullable String reason,
                            GatewayDiscordClient gateway, long channelId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.channelId = channelId;
    }

    public InviteCreateMono(GatewayDiscordClient gateway, long channelId) {
        this(InviteCreateRequest::builder, null, gateway, channelId);
    }

    @Override
    InviteCreateMono withBuilder(UnaryOperator<ImmutableInviteCreateRequest.Builder> f) {
        return new InviteCreateMono(apply(f), reason, gateway, channelId);
    }

    public InviteCreateMono withMaxAge(int maxAge) {
        return withBuilder(it -> it.maxAge(maxAge));
    }

    public InviteCreateMono withMaxUses(int maxUses) {
        return withBuilder(it -> it.maxUses(maxUses));
    }

    public InviteCreateMono withTemporary(boolean temporary) {
        return withBuilder(it -> it.temporary(temporary));
    }

    public InviteCreateMono withUnique(boolean unique) {
        return withBuilder(it -> it.unique(unique));
    }

    @Override
    public InviteCreateMono withReason(String reason) {
        return new InviteCreateMono(requestBuilder, reason, gateway, channelId);
    }

    @Override
    Mono<ExtendedInvite> getRequest() {
        return gateway.getRestClient().getChannelService()
                .createChannelInvite(channelId, requestBuilder.get().build(), reason)
                .map(data -> new ExtendedInvite(gateway, data));
    }
}
