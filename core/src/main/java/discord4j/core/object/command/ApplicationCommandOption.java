package discord4j.core.object.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord application command option.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommandoption">
 *     Application Command Option Object</a>
 */
public class ApplicationCommandOption implements DiscordObject {

    /** The maximum amount of characters that can be in an application command option name. */
    public static final int MAX_NAME_LENGTH = 32;
    /** The maximum amount of characters that can be in an application command option description. */
    public static final int MAX_DESCRIPTION_LENGTH = 100;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandOptionData data;

    /**
     * Constructs an {@code ApplicationCommandOption} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandOption(final GatewayDiscordClient gateway, final ApplicationCommandOptionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the type of the option.
     *
     * @return The type of the option.
     */
    public Type getType() {
        return Type.of(data.type());
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
     * Gets the description of the option.
     *
     * @return The description of the option.
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets whether this option is required.
     *
     * @return Whether this option is required.
     */
    public boolean isRequired() {
        return data.required().toOptional().orElse(false);
    }

    /**
     * Gets the choices for {@code string} and {@code int} types for the user to pick from.
     *
     * @return The choices for {@code string} and {@code int} types for the user to pick from.
     */
    public List<ApplicationCommandOptionChoice> getChoices() {
        return data.choices().toOptional().orElse(Collections.emptyList())
            .stream()
            .map(data -> new ApplicationCommandOptionChoice(gateway, data))
            .collect(Collectors.toList());
    }

    /**
     * Gets the choice corresponding to the provided name, if present.
     *
     * @param name The name of the choice.
     * @return The choice corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandOptionChoice> getChoice(final String name) {
        return getChoices().stream()
            .filter(choice -> choice.getName().equals(name))
            .findFirst();
    }

    /**
     * Gets the options of this option, if the option is a subcommand or subcommand group type.
     *
     * @return The options of this option, if the option is a subcommand or subcommand group type.
     */
    public List<ApplicationCommandOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList())
            .stream()
            .map(data -> new ApplicationCommandOption(gateway, data))
            .collect(Collectors.toList());
    }

    /**
     * Gets the option of this option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option of this option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandOption> getOption(final String name) {
        return getOptions().stream()
            .filter(option -> option.getName().equals(name))
            .findFirst();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /** Represents the various types of options. */
    public enum Type {

        UNKNOWN(-1),
        SUB_COMMAND(1),
        SUB_COMMAND_GROUP(2),
        STRING(3),
        INTEGER(4),
        BOOLEAN(5),
        USER(6),
        CHANNEL(7),
        ROLE(8);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs an {@code ApplicationCommandOption.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of option. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of option.
         */
        public static ApplicationCommandOption.Type of(final int value) {
            return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(UNKNOWN);
        }
    }
}
