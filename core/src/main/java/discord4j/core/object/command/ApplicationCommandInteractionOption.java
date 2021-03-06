package discord4j.core.object.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
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
public class ApplicationCommandInteractionOption implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandInteractionOptionData data;

    @Nullable
    private final Long guildId;

    /**
     * Constructs an {@code ApplicationCommandInteractionOption} with an associated {@link GatewayDiscordClient} and
     * Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandInteractionOption(final GatewayDiscordClient gateway,
                                               final ApplicationCommandInteractionOptionData data,
                                               @Nullable final Long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    /**
     * Gets the name of the option.
     *
     * @return The name of the option.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the value of this option as a string, present if this option is not a group or subcommand.
     *
     * @return The value of this option as a string, present if this option is not a group or subcommand.
     */
    public Optional<String> getValueAsString() {
        return data.value().toOptional();
    }

    /**
     * Gets the value of this option as a boolean, present if this option is not a group or subcommand.
     *
     * @return The value of this option as a boolean, present if this option is not a group or subcommand.
     * @throws IllegalArgumentException if the type is not boolean
     */
    public Optional<Boolean> getValueAsBoolean() {
        if (getType() != ApplicationCommandOption.Type.BOOLEAN) {
            // TODO
            throw new IllegalArgumentException("Option value cannot be converted as boolean");
        }

        return getValueAsString().map(Boolean::parseBoolean);
    }

    /**
     * Gets the value of this option as a long, present if this option is not a group or subcommand.
     *
     * @return The value of this option as a long, present if this option is not a group or subcommand.
     * @throws IllegalArgumentException if the type is not integer
     */
    public Optional<Long> getValueAsLong() {
        if (getType() != ApplicationCommandOption.Type.INTEGER) {
            // TODO
            throw new IllegalArgumentException("Option value cannot be converted as long");
        }

        return getValueAsString().map(Long::parseLong);
    }

    /**
     * Gets the value of this option as a {@link Snowflake}, present if this option is not a group or subcommand.
     *
     * @return The value of this option as a {@link Snowflake}, present if this option is not a group or subcommand.
     * @throws IllegalArgumentException if the type is not user, channel or role
     */
    public Optional<Snowflake> getValueAsSnowflake() {
        if (getType() != ApplicationCommandOption.Type.USER
                && getType() != ApplicationCommandOption.Type.ROLE
                && getType() != ApplicationCommandOption.Type.CHANNEL) {
            // TODO
            throw new IllegalArgumentException("Option value cannot be converted as snowflake");
        }

        return getValueAsString().map(Snowflake::of);
    }

    /**
     * Gets the type of this option.
     *
     * @return The type of this option.
     */
    public ApplicationCommandOption.Type getType() {
        return ApplicationCommandOption.Type.of(data.type());
    }

    /**
     * Gets the options of this option, present if this option is a group or subcommand.
     *
     * @return The options of this option, present if this option is a group or subcommand.
     */
    public List<ApplicationCommandInteractionOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList()).stream()
                .map(data -> new ApplicationCommandInteractionOption(gateway, data, guildId))
                .collect(Collectors.toList());
    }

    /**
     * Gets the option of this option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option of this option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandInteractionOption> getOption(final String name) {
        return getOptions().stream()
                .filter(data -> data.getName().equals(name))
                .findFirst();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
