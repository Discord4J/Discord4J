package discord4j.core.object.monetization;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.discordjson.json.SkuData;

import java.util.EnumSet;

/**
 * A Discord SKU.
 *
 * @see <a href="https://discord.com/developers/docs/monetization/skus">Discord Documentation</a>
 */
@Experimental // These methods could not be tested due to the lack of a Discord verified application
public class SKU implements Entity {

    private static final String SKU_URL_SCHEME = "https://discord.com/application-directory/%s/store/%s";

    private final GatewayDiscordClient gateway;
    private final SkuData data;

    /**
     * Constructs a {@code SKU} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object.
     * @param data The raw data as represented by Discord.
     */
    public SKU(final GatewayDiscordClient gateway, final SkuData data) {
        this.gateway = gateway;
        this.data = data;
    }

    /**
     * Gets the type of SKU.
     *
     * @return The type of SKU.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the name of the SKU.
     *
     * @return The name of the SKU.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the application ID associated with this SKU.
     *
     * @return The application ID associated with this SKU.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(data.applicationId());
    }

    /**
     * Gets the SKU's slug.
     *
     * @return The SKU's slug.
     */
    public String getSlug() {
        return data.slug();
    }

    /**
     * Gets the flags of the SKU.
     *
     * @return An {@link EnumSet} of flags.
     */
    public EnumSet<Flag> getFlags() {
        if (data.flags() != 0) {
            return Flag.of(data.flags());
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Gets the data of the SKU.
     *
     * @return The data of the SKU.
     */
    public SkuData getSkuData() {
        return data;
    }

    /**
     * Get the URL of the SKU.
     *
     * @return The URL of the SKU.
     */
    public String getUrl() {
        return String.format(SKU.SKU_URL_SCHEME, data.applicationId().asString(), data.id().asString());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    public enum Type {

        /* Unknown type */
        UNKNOWN(-1),

        /* Durable one-time purchase */
        DURABLE(2),

        /* Consumable one-time purchase */
        CONSUMABLE(3),

        /* Represents a recurring subscription */
        SUBSCRIPTION(5),

        /* System-generated group for each SUBSCRIPTION SKU created */
        SUBSCRIPTION_GROUP(6);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code SKU.Type}.
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
         * Gets the type of SKU. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of SKU.
         */
        public static Type of(final int value) {
            switch (value) {
                case 2:
                    return DURABLE;
                case 3:
                    return CONSUMABLE;
                case 5:
                    return SUBSCRIPTION;
                case 6:
                    return SUBSCRIPTION_GROUP;
                default:
                    return UNKNOWN;
            }
        }
    }

    public enum Flag {

        /* SKU is available for purchase */
        AVAILABLE(2),

        /* Recurring SKU that can be purchased by a user and applied to a single server. Grants access to every user in that server. */
        GUILD_SUBSCRIPTION(7),

        /* Recurring SKU purchased by a user for themselves. Grants access to the purchasing user in every server. */
        USER_SUBSCRIPTION(8),
        ;

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code SKU.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
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
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of a SKU. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> flagSet = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flagSet.add(flag);
                }
            }
            return flagSet;
        }

    }
}
