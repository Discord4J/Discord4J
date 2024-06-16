package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.BulkBanResponseData;
import discord4j.discordjson.json.RegionData;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Bulk Ban response.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#bulk-guild-ban-bulk-ban-response">Bulk Ban Object</a>
 */
public class BulkBan implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final BulkBanResponseData data;

    /**
     * Constructs a {@code BulkBan} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public BulkBan(final GatewayDiscordClient gateway, final BulkBanResponseData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the bulk ban response.
     *
     * @return The data of the bulk ban response.
     */
    public BulkBanResponseData getData() {
        return data;
    }

    /**
     * Gets the successful banned users ids.
     *
     * @return A list with all the user ids.
     */
    public List<Snowflake> getBannedUserIds() {
        return data.bannedUsers().stream().map(Snowflake::of).collect(Collectors.toList());
    }

    /**
     * Gets the unsuccessful banned users ids.
     *
     * @return A list with all the user ids.
     */
    public List<Snowflake> getFailedUserIds() {
        return data.failedUsers().stream().map(Snowflake::of).collect(Collectors.toList());
    }
}
