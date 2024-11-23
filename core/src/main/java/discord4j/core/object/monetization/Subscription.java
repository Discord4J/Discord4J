package discord4j.core.object.monetization;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.SubscriptionData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Subscription implements Entity {

    private final GatewayDiscordClient gateway;
    private final SubscriptionData data;

    public Subscription(GatewayDiscordClient gateway, SubscriptionData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(this.data.id());
    }

    public SubscriptionData getData() {
        return this.data;
    }

    /**
     * Gets the current status of the subscription.
     *
     * @return The current status of the subscription.
     */
    public Status getStatus() {
        return Status.of(this.data.status());
    }

    /**
     * Gets the country code of the payment source used to purchase the subscription. Missing unless queried with a
     * private OAuth scope.
     *
     * @return An ISO3166-1 alpha-2 country code.
     */
    public Optional<String> getCountry() {
        return this.data.country().toOptional();
    }

    /**
     * Gets the start of the current subscription period.
     *
     * @return The start of the current subscription period.
     */
    public Instant getCurrentPeriodStart() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.data.currentPeriodStart(), Instant::from);
    }

    /**
     * Gets the end of the current subscription period.
     *
     * @return The end of the current subscription period.
     */
    public Instant getCurrentPeriodEnd() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.data.currentPeriodEnd(), Instant::from);
    }

    /**
     * Gets when the subscription was canceled.
     *
     * @return When the subscription was canceled.
     */
    public Optional<Instant> getCanceledAt() {
        return this.data.canceledAt().map(it -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(it, Instant::from));
    }

    /**
     * Gets the ID of the user who is subscribed.
     *
     * @return The ID of the user who is subscribed.
     */
    public Snowflake getUserId() {
        return Snowflake.of(this.data.userId());
    }

    /**
     * Gets the user who is subscribed.
     *
     * @return The user who is subscribed.
     */
    public Mono<User> getUser() {
        return this.gateway.getUserById(this.getUserId());
    }

    /**
     * Gets the SKU Ids related to this Subscription.
     *
     * @return A list of SKU Ids related to this Subscription.
     */
    public List<Snowflake> getSkusId() {
        return this.data.skuIds().stream().map(Snowflake::of).collect(Collectors.toList());
    }

    /**
     * Gets the SKU related to this Subscription.
     *
     * @return A Flux of SKU related to this Subscription.
     */
    public Flux<SKU> getSkus() {
        List<Snowflake> skusId = this.getSkusId();
        return this.gateway.getSKUs().filter(sku -> skusId.contains(sku.getId()));
    }

    /**
     * Gets the entitlement ids related to this Subscription.
     *
     * @return A list of entitlement ids related to this Subscription.
     */
    public List<Snowflake> getEntitlementId() {
        return this.data.entitlementIds().stream().map(Snowflake::of).collect(Collectors.toList());
    }

    /**
     * Represents the status of a Subscription.
     *
     * @see <a href="https://discord.com/developers/docs/resources/subscription#subscription-statuses">
     * Subscription Statuses</a>
     */
    public enum Status {
        /**
         * Unknown status
         */
        UNKNOWN(-1),

        /**
         * Subscription is active and scheduled to renew.
         */
        ACTIVE(0),

        /**
         * Subscription is active but will not renew.
         */
        ENDING(1),

        /**
         * Subscription is inactive and not being charged.
         */
        INACTIVE(2),
        ;

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs an {@code Status}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Status(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the status. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The status.
         */
        public static Status of(final int value) {
            switch (value) {
                case 0:
                    return ACTIVE;
                case 1:
                    return ENDING;
                case 2:
                    return INACTIVE;
                default:
                    return UNKNOWN;
            }
        }
    }

}
