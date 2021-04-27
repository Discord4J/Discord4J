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
