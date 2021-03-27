package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Discord application command interaction.
 *
 * @see
 * <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction-applicationcommandinteractiondata">
 * Application Command Interaction Object</a>
 */
@Experimental
public class ApplicationCommandInteraction implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandInteractionData data;

    @Nullable
    private final Long guildId;

    /**
     * Constructs an {@code ApplicationCommandInteraction} with an associated {@link GatewayDiscordClient} and
     * Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandInteraction(final GatewayDiscordClient gateway,
                                         final ApplicationCommandInteractionData data,
                                         @Nullable final Long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    /**
     * Gets	the id of the invoked command.
     *
     * @return The id of the invoked command.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the options of the invoked command.
     *
     * @return The options of the invoked command.
     */
    public List<ApplicationCommandInteractionOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList()).stream()
                .map(data -> new ApplicationCommandInteractionOption(gateway, data, guildId))
                .collect(Collectors.toList());
    }

    /**
     * Gets the option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandInteractionOption> getOption(final String name) {
        return getOptions().stream()
                .filter(option -> option.getName().equals(name))
                .findFirst();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
