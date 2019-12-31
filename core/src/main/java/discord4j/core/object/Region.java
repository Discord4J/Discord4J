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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.data.RegionBean;

import java.util.Objects;

/**
 * A Discord voice region.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/voice#voice-region-object">Voice Region Object</a>
 */
public final class Region implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final RegionBean data;

    /**
     * Constructs a {@code Region} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Region(final GatewayDiscordClient gateway, final RegionBean data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the unique ID for the region.
     *
     * @return The unique ID for the region.
     */
    public String getId() {
        return data.getId();
    }

    /**
     * Gets the name of the region.
     *
     * @return The name of the region.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets an example hostname for the region.
     *
     * @return An example hostname for the region.
     */
    public String getSampleHostname() {
        return data.getSampleHostname();
    }

    /**
     * Gets an example port for the region.
     *
     * @return An example port for the region.
     */
    public int getSamplePort() {
        return data.getSamplePort();
    }

    /**
     * Gets if this is a VIP region.
     *
     * @return {@code true} if this is a VIP region, {@code false} otherwise.
     */
    public boolean isVip() {
        return data.isVip();
    }

    /**
     * Gets if the region is closest to the current user's serviceMediator.
     *
     * @return {@code true} if the region is closest to the current user's serviceMediator, {@code false} otherwise.
     */
    public boolean isOptimal() {
        return data.isOptimal();
    }

    /**
     * Gets if this is a deprecated voice region.
     *
     * @return {@code true} if this is a deprecated voice region, {@code false} otherwise.
     */
    public boolean isDeprecated() {
        return data.isDeprecated();
    }

    /**
     * Gets if this is a custom voice region.
     *
     * @return {@code true} if this is a custom voice region, {@code false} otherwise.
     */
    public boolean isCustom() {
        return data.isCustom();
    }

    @Override
    public String toString() {
        return "Region{" +
                "data=" + data +
                '}';
    }
}
