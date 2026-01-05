/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.discordjson.json.RegionData;
import discord4j.core.GatewayDiscordClient;

import java.util.Objects;

/**
 * A Discord voice region.
 *
 * @see <a href="https://discord.com/developers/docs/resources/voice#voice-region-object">Voice Region Object</a>
 */
public final class Region implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final RegionData data;

    /**
     * Constructs a {@code Region} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Region(final GatewayDiscordClient gateway, final RegionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the region.
     *
     * @return The data of the region.
     */
    public RegionData getData() {
        return data;
    }

    /**
     * Gets the unique ID for the region.
     *
     * @return The unique ID for the region.
     */
    public String getId() {
        return data.id();
    }

    /**
     * Gets the name of the region.
     *
     * @return The name of the region.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets if this is a VIP region.
     *
     * @return {@code true} if this is a VIP region, {@code false} otherwise.
     */
    public boolean isVip() {
        // TODO: workaround, see https://github.com/Discord4J/Discord4J/issues/999
        return data.vip().toOptional().orElse(false);
    }

    /**
     * Gets if the region is closest to the current user's serviceMediator.
     *
     * @return {@code true} if the region is closest to the current user's serviceMediator, {@code false} otherwise.
     */
    public boolean isOptimal() {
        return data.optimal();
    }

    /**
     * Gets if this is a deprecated voice region.
     *
     * @return {@code true} if this is a deprecated voice region, {@code false} otherwise.
     */
    public boolean isDeprecated() {
        return data.deprecated();
    }

    /**
     * Gets if this is a custom voice region.
     *
     * @return {@code true} if this is a custom voice region, {@code false} otherwise.
     */
    public boolean isCustom() {
        return data.custom();
    }

    @Override
    public String toString() {
        return "Region{" +
                "data=" + data +
                '}';
    }

    /** Represents the different non-deprecated voice region ids. */
    public enum Id {

        UNKNOWN(null),

        AUTOMATIC(null),

        US_WEST("us-west"),

        US_EAST("us-east"),

        US_CENTRAL("us-central"),

        US_SOUTH("us-south"),

        SINGAPORE("singapore"),

        SOUTHAFRICA("southafrica"),

        SYDNEY("sydney"),

        EUROPE("europe"),

        BRAZIL("brazil"),

        HONGKONG("hongkong"),

        RUSSIA("russia"),

        JAPAN("japan"),

        INDIA("india");

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs a {@code Region.Id}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Id(@Nullable final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        @Nullable
        public String getValue() {
            return value;
        }

        /**
         * Gets the enum associated with the value. It is guaranteed that invoking {@link #getValue()} from the returned
         * enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The region id.
         */
        public static Region.Id of(@Nullable final String value) {
            if(value == null) {
                return AUTOMATIC;
            }

            switch (value) {
                case "us-west": return US_WEST;
                case "us-east": return US_EAST;
                case "us-central": return US_CENTRAL;
                case "us-south": return US_SOUTH;
                case "singapore": return SINGAPORE;
                case "southafrica": return SOUTHAFRICA;
                case "sydney": return SYDNEY;
                case "europe": return EUROPE;
                case "brazil": return BRAZIL;
                case "hongkong": return HONGKONG;
                case "russia": return RUSSIA;
                case "japan": return JAPAN;
                case "india": return INDIA;
                default: return UNKNOWN;
            }
        }
    }
}
