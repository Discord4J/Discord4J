package discord4j.core.object.monetization;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.discordjson.json.EntitlementData;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a Discord entitlement.
 *
 * @see <a href="https://discord.com/developers/docs/monetization/entitlements">Discord Documentation</a>
 */
public class Entitlement implements Entity {

    private final GatewayDiscordClient gateway;
    private final EntitlementData data;

    /**
     * Constructs an {@code Entitlement} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object.
     * @param data The raw data as represented by Discord.
     */
    public Entitlement(final GatewayDiscordClient gateway, final EntitlementData data) {
        this.gateway = gateway;
        this.data = data;
    }

    /**
     * Gets the SKU ID associated with this entitlement.
     *
     * @return The SKU ID associated with this entitlement.
     */
    public Snowflake getSkuId() {
        return Snowflake.of(data.skuId());
    }

    /**
     * Gets the application ID associated with this entitlement.
     *
     * @return The application ID associated with this entitlement.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(data.applicationId());
    }

    /**
     * Gets the user ID of the entitlement.
     * Only present if the entitlement corresponds to a user subscription.
     *
     * @return An {@link Optional} containing the user ID of the entitlement.
     */
    public Optional<Snowflake> getUserId() {
        return data.userId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the guild ID of the entitlement.
     * Only present if the entitlement corresponds to a guild subscription.
     *
     * @return An {@link Optional} containing the guild ID of the entitlement.
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the owner ID of the entitlement. This is either the user ID or guild ID,
     * depending on the type of entitlement.
     *
     * @return An {@link Optional} containing the owner ID of the entitlement.
     */
    public Optional<Snowflake> getOwnerId() {
        Optional<Snowflake> userId = getUserId();
        if (userId.isPresent()) {
            return userId;
        }
        return getGuildId();
    }

    /**
     * Gets the type of entitlement.
     *
     * @return The type of entitlement.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets whether the entitlement has been deleted.
     *
     * @return Whether the entitlement has been deleted.
     */
    public boolean isDeleted() {
        return data.deleted();
    }

    /**
     * Gets the start time of the entitlement.
     * Not present for test entitlements.
     *
     * @return An {@link Optional} containing the start time of the entitlement.
     */
    public Optional<Instant> getStartsAt() {
        return data.startsAt().toOptional().map(Instant::parse);
    }

    /**
     * Gets the end time of the entitlement.
     * Not present for test entitlements.
     *
     * @return An {@link Optional} containing the end time of the entitlement.
     */
    public Optional<Instant> getEndsAt() {
        return data.endsAt().toOptional().map(Instant::parse);
    }

    /**
     * Gets the subscription ID of the entitlement.
     * Not present for test entitlements.
     *
     * @return An {@link Optional} containing the subscription ID of the entitlement.
     */
    public Optional<Snowflake> getSubscriptionId() {
        return data.subscriptionId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets whether the entitlement has been consumed.
     *
     * @return Whether the entitlement has been consumed.
     */
    public boolean isConsumed() {
        return data.consumed().toOptional().orElse(false);
    }

    /**
     * Gets the data of the entitlement.
     *
     * @return The data of the entitlement.
     */
    public EntitlementData getData() {
        return data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    public enum OwnerType {

        /* Unknown owner type */
        UNKNOWN(-1),

        /* Represents a guild */
        GUILD(1),

        /* Represents a user */
        USER(2);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs an {@code OwnerType}.
         *
         * @param value The underlying value as represented by Discord.
         */
        OwnerType(final int value) {
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
         * Gets the owner type. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The owner type.
         */
        public static OwnerType of(final int value) {
            switch (value) {
                case 1:
                    return GUILD;
                case 2:
                    return USER;
                default:
                    return UNKNOWN;
            }
        }

    }

    public enum Type {

        /* Unknown type */
        UNKNOWN(-1),

        /* Entitlement was purchased by user */
        PURCHASE(1),

        /* Entitlement for Discord Nitro subscription */
        PREMIUM_SUBSCRIPTION(2),

        /* Entitlement was gifted by developer */
        DEVELOPER_GIFT(3),

        /* Entitlement was purchased by a dev in application test mode */
        TEST_MODE_PURCHASE(4),

        /* Entitlement was granted when the SKU was free */
        FREE_PURCHASE(5),

        /* Entitlement was gifted by another user */
        USER_GIFT(6),

        /* Entitlement was claimed by user for free as a Nitro Subscriber */
        PREMIUM_PURCHASE(7),

        /* Represents a recurring subscription */
        APPLICATION_SUBSCRIPTION(8);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
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
         * Gets the type of Entitlement. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of Entitlement.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1:
                    return PURCHASE;
                case 2:
                    return PREMIUM_SUBSCRIPTION;
                case 3:
                    return DEVELOPER_GIFT;
                case 4:
                    return TEST_MODE_PURCHASE;
                case 5:
                    return FREE_PURCHASE;
                case 6:
                    return USER_GIFT;
                case 7:
                    return PREMIUM_PURCHASE;
                case 8:
                    return APPLICATION_SUBSCRIPTION;
                default:
                    return UNKNOWN;
            }
        }
    }

}
