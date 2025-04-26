package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.monetization.Entitlement;
import discord4j.core.object.monetization.Subscription;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Multimap;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.setIfPresent;

@Value.Immutable
public interface SubscriptionListRequestSpecGenerator extends Spec<Multimap<String, Object>> {

    @JsonProperty("user_id")
    Possible<Snowflake> userId();

    Possible<Snowflake> before();

    Possible<Snowflake> after();

    Possible<Integer> limit();

    @Override
    default Multimap<String, Object> asRequest() {
        Multimap<String, Object> map = new Multimap<>();

        setIfPresent(map, "user_id", userId());
        setIfPresent(map, "before", before());
        setIfPresent(map, "after", after());
        setIfPresent(map, "limit", limit());

        return map;
    }

}

@Value.Immutable(builder = false)
abstract class SubscriptionListRequestFluxGenerator extends Flux<Subscription> implements SubscriptionListRequestSpecGenerator {

    abstract GatewayDiscordClient client();

    abstract DiscordClient restClient();

    abstract Snowflake entitlementId();

    @Override
    public void subscribe(CoreSubscriber<? super Subscription> coreSubscriber) {
        Mono.justOrEmpty(entitlementId())
            .flatMapMany(entitlementId -> restClient()
                .getMonetizationService()
                .getSkuSubscriptions(entitlementId.asLong(), this.asRequest())
                .map(data -> new Subscription(client(), data)))
            .subscribe(coreSubscriber);
    }

    @Override
    public abstract String toString();
}

