package discord4j.core.event.domain.monetization;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.User;
import discord4j.core.object.monetization.Subscription;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a subscription is updated.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#subscription-update">Update Subscription</a>
 */
@Experimental // These methods could not be tested due to the lack of a Discord verified application
public class SubscriptionUpdateEvent extends Event {

    private final Subscription subscription;

    public SubscriptionUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Subscription subscription) {
        super(gateway, shardInfo);

        this.subscription = subscription;
    }

    /**
     * Gets the subscription that was updated.
     *
     * @return The subscription that was updated.
     */
    public Subscription getSubscription() {
        return subscription;
    }

    /**
     * Gets the user associated with this subscription.
     *
     * @return A {@link Mono} where, upon successful completion, emits the user associated with this subscription.
     */
    public Mono<User> getUser() {
        return subscription.getUser();
    }

}
