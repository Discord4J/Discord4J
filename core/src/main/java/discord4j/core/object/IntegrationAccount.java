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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.IntegrationAccountData;

import java.util.Objects;

/**
 * A Discord Integration Account.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#integration-account-object">Account Resource</a>
 */
public class IntegrationAccount implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final IntegrationAccountData data;

    /**
     * Constructs a {@code IntegrationAccount} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public IntegrationAccount(final GatewayDiscordClient gateway, final IntegrationAccountData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the account.
     *
     * @return The data of the account.
     */
    public IntegrationAccountData getData() {
        return data;
    }

    /**
     * Gets the id of the account.
     *
     * @return The id of the account.
     */
    public String getId() {
        return data.id();
    }

    /**
     * Gets the name of the account.
     *
     * @return The name of the account.
     */
    public String getName() {
        return data.name();
    }
}
