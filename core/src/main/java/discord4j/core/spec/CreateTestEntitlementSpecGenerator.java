package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.monetization.Entitlement;
import discord4j.discordjson.json.CreateTestEntitlementRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Value.Immutable
public interface CreateTestEntitlementSpecGenerator extends Spec<CreateTestEntitlementRequest> {

    @JsonProperty("sku_id")
    Snowflake skuId();

    @JsonProperty("owner_id")
    Snowflake ownerId();

    @JsonProperty("owner_type")
    Entitlement.OwnerType ownerType();

    @Override
    default CreateTestEntitlementRequest asRequest() {
        return CreateTestEntitlementRequest.builder()
            .skuId(skuId().asLong())
            .ownerId(ownerId().asLong())
            .ownerType(ownerType().getValue())
            .build();
    }

}

@Value.Immutable(builder = false)
abstract class CreateTestEntitlementMonoGenerator extends Mono<Entitlement> implements CreateTestEntitlementSpecGenerator {

    abstract GatewayDiscordClient client();

    abstract DiscordClient restClient();

    @Override
    public void subscribe(CoreSubscriber<? super Entitlement> coreSubscriber) {
        restClient().getApplicationId()
            .flatMapMany(applicationId -> restClient()
                .getMonetizationService()
                .createTestEntitlement(applicationId, this.asRequest())
                .map(data -> new Entitlement(client(), data)))
            .subscribe(coreSubscriber);
    }

    @Override
    public abstract String toString();
}
