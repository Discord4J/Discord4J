package discord4j.core.object.reaction;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ReactionCountDetailsData;
import discord4j.discordjson.json.ReactionData;

import java.util.Objects;

/**
 * A Discord reaction count detail.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#reaction-count-details-object">Reaction Count Details Object</a>
 */
public class ReactionCountDetails implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ReactionCountDetailsData data;

    /**
     * Constructs a {@code ReactionCountDetails} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ReactionCountDetails(final GatewayDiscordClient gateway, final ReactionCountDetailsData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets the data of the reaction.
     *
     * @return The data of the reaction.
     */
    public ReactionCountDetailsData getData() {
        return data;
    }

    /**
     * Count of normal reactions
     *
     * @return Count of normal reactions
     */
    public int getNormal() {
        return this.data.normal();
    }

    /**
     * Count of super reactions
     *
     * @return Count of super reactions
     */
    public int getSuper() {
        return this.data.burst();
    }
}
