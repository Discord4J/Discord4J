package discord4j.core.object.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;

import java.util.Objects;

/**
 * A Discord application command option choice.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommandoptionchoice">
 *     Application Command Option Choice Object</a>
 */
public class ApplicationCommandOptionChoice implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandOptionChoiceData data;

    /**
     * Constructs an {@code ApplicationCommandOptionChoice} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandOptionChoice(final GatewayDiscordClient gateway, final ApplicationCommandOptionChoiceData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the name of this choice.
     *
     * @return The name of this choice.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the value of this choice as a string.
     *
     * @return The value of this choice as a string.
     */
    public String asString() {
        return data.value();
    }

    /**
     * Gets the value of this choice as an int.
     *
     * @return The value of this choice as an int.
     */
    public int asInt() {
        // TODO: Exceptions handling
        return Integer.parseInt(data.value());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
