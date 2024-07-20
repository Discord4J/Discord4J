package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.monetization.Entitlement;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Multimap;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.setIfPresent;

@Value.Immutable
public interface EntitlementListRequestSpecGenerator extends Spec<Multimap<String, Object>> {

    @JsonProperty("user_id")
    Possible<Snowflake> userId();

    @JsonProperty("sku_ids")
    Possible<List<Snowflake>> skuIds();

    Possible<Snowflake> before();

    Possible<Snowflake> after();

    Possible<Integer> limit();

    @JsonProperty("guild_id")
    Possible<Snowflake> guildId();

    @JsonProperty("exclude_ended")
    Possible<Boolean> excludeEnded();

    @Override
    default Multimap<String, Object> asRequest() {
        Multimap<String, Object> map = new Multimap<>();

        setIfPresent(map, "user_id", userId());
        setIfPresent(map, "sku_ids", mapPossible(skuIds(), snowflakes -> snowflakes.stream().map(Snowflake::asString).collect(Collectors.joining(","))));
        setIfPresent(map, "before", before());
        setIfPresent(map, "after", after());
        setIfPresent(map, "limit", limit());
        setIfPresent(map, "guild_id", guildId());
        setIfPresent(map, "exclude_ended", excludeEnded());

        return map;
    }

}

@Value.Immutable(builder = false)
abstract class EntitlementListRequestFluxGenerator extends Flux<Entitlement> implements EntitlementListRequestSpecGenerator {

    abstract GatewayDiscordClient client();

    abstract DiscordClient restClient();

    @Override
    public void subscribe(CoreSubscriber<? super Entitlement> coreSubscriber) {
        restClient().getApplicationId()
            .flatMapMany(applicationId -> restClient()
                .getMonetizationService()
                .getAllEntitlements(applicationId, this.asRequest())
                .map(data -> new Entitlement(client(), data)))
            .subscribe(coreSubscriber);
    }

    @Override
    public abstract String toString();
}
