package discord4j.core.event.dispatch;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.monetization.EntitlementCreateEvent;
import discord4j.core.object.monetization.Entitlement;
import discord4j.discordjson.json.EntitlementData;
import discord4j.discordjson.json.gateway.EntitlementCreate;
import discord4j.discordjson.json.gateway.EntitlementDelete;
import discord4j.discordjson.json.gateway.EntitlementUpdate;
import reactor.core.publisher.Mono;

public class MonetizationDispatchHandlers {

    static Mono<EntitlementCreateEvent> entitlementCreate(DispatchContext<EntitlementCreate, Void> context) {
        EntitlementData entitlementData = context.getDispatch().entitlement();

        return Mono.just(new EntitlementCreateEvent(context.getGateway(), context.getShardInfo(), new Entitlement(context.getGateway(), entitlementData)));
    }

    static Mono<EntitlementCreateEvent> entitlementUpdate(DispatchContext<EntitlementUpdate, Void> context) {
        EntitlementData entitlementData = context.getDispatch().entitlement();

        return Mono.just(new EntitlementCreateEvent(context.getGateway(), context.getShardInfo(), new Entitlement(context.getGateway(), entitlementData)));
    }

    static Mono<EntitlementCreateEvent> entitlementDelete(DispatchContext<EntitlementDelete, Void> context) {
        EntitlementData entitlementData = context.getDispatch().entitlement();

        return Mono.just(new EntitlementCreateEvent(context.getGateway(), context.getShardInfo(), new Entitlement(context.getGateway(), entitlementData)));
    }

}
