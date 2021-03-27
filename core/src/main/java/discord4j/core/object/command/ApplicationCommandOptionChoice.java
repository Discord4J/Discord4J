package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;

import java.util.Objects;

/**
 * A Discord application command option choice.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommandoptionchoice">
 * Application Command Option Choice Object</a>
 */
@Experimental
public class ApplicationCommandOptionChoice implements DiscordObject {

    /** The maximum amount of characters that can be in an application command option choice name. */
    public static final int MAX_NAME_LENGTH = 100;
    /** The maximum amount of characters that can be in an application command option choice value. */
    public static final int MAX_VALUE_LENGTH = 100;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandOptionChoiceData data;

    /**
     * Constructs an {@code ApplicationCommandOptionChoice} with an associated {@link GatewayDiscordClient} and
     * Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandOptionChoice(final GatewayDiscordClient gateway,
                                          final ApplicationCommandOptionChoiceData data) {
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
     * Gets the value of this choice as a long.
     *
     * @return The value of this choice as a long.
     */
    public long asLong() {
        try {
            return Long.parseLong(data.value());
        } catch (NumberFormatException err) {
            throw new IllegalArgumentException("Choice value cannot be converted to long");
        }
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
