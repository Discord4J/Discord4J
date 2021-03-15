package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.MessageInteractionData;
import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.Objects;

/**
 * A Discord Message Interaction.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#messageinteraction">
 * Message Interaction Object</a>
 */
public class MessageInteraction implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageInteractionData data;

    /**
     * Constructs a {@code MessageInteraction} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageInteraction(final GatewayDiscordClient gateway, final MessageInteractionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the id of the interaction.
     *
     * @return The id of the interaction.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the type of interaction.
     *
     * @return The type of interaction
     */
    public ApplicationCommandOptionType getType() {
        return ApplicationCommandOptionType.of(data.type());
    }

    /**
     * Gets the name of the {@link discord4j.core.object.command.ApplicationCommand}.
     *
     * @return The name of the {@link discord4j.core.object.command.ApplicationCommand}.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the user who invoked the interaction.
     *
     * @return The user who invoked the interaction.
     */
    public User getUser() {
        return new User(gateway, data.user());
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

}
