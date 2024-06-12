package discord4j.rest.service;

import discord4j.common.annotations.Experimental;
import discord4j.discordjson.json.CreateTestEntitlementRequest;
import discord4j.discordjson.json.EntitlementData;
import discord4j.discordjson.json.SkuData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.Multimap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Experimental // These methods could not be tested due to the lack of a Discord verified application
public class MonetizationService extends RestService {

    public MonetizationService(Router router) {
        super(router);
    }

    public Flux<SkuData> getAllSkus(long applicationId) {
        return Routes.LIST_SKUS.newRequest(applicationId)
            .exchange(getRouter())
            .bodyToMono(SkuData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<EntitlementData> getAllEntitlements(long applicationId) {
        return getAllEntitlements(applicationId, new Multimap<>());
    }

    public Flux<EntitlementData> getAllEntitlements(long applicationId, Multimap<String, Object> params) {
        return Routes.LIST_ENTITLEMENTS.newRequest(applicationId)
            .query(params)
            .exchange(getRouter())
            .bodyToMono(EntitlementData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<EntitlementData> createTestEntitlement(long applicationId, CreateTestEntitlementRequest request) {
        return Routes.CREATE_TEST_ENTITLEMENT.newRequest(applicationId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(EntitlementData.class);
    }

    public Mono<Void> deleteTestEntitlement(long applicationId, long entitlementId) {
        return Routes.DELETE_TEST_ENTITLEMENT.newRequest(applicationId, entitlementId)
            .exchange(getRouter())
            .skipBody();
    }

    public Mono<Void> consumeEntitlement(long applicationId, long entitlementId) {
        return Routes.CONSUME_ENTITLEMENT.newRequest(applicationId, entitlementId)
            .exchange(getRouter())
            .skipBody();
    }

}
