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

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Discord application command option.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommandoption">
 * Application Command Option Object</a>
 */
@Experimental
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
    public /*~~>*/List<ApplicationCommandOptionChoice> getChoices() {
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
     * Gets the options, if the option is a subcommand or subcommand group type.
     *
     * @return The options, if the option is a subcommand or subcommand group type.
     */
    public /*~~>*/List<ApplicationCommandOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList())
                .stream()
                .map(data -> new ApplicationCommandOption(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets the option corresponding to the provided name, if present and if this option is a subcommand or
     * subcommand group type.
     *
     * @param name The name of the option.
     * @return The option corresponding to the provided name, if present and if this option is a subcommand or
     * subcommand group type.
     */
    public Optional<ApplicationCommandOption> getOption(final String name) {
        return getOptions().stream()
                .filter(option -> option.getName().equals(name))
                .findFirst();
    }

    /**
     * Returns a list of acceptable channel types the user may pick
     * </p>
     * Only applies to CHANNEL type options, if empty, no restriction on channel types is placed.
     * @return A list of channel types a user may pick. Empty list means no restriction is applied.
     */
    public /*~~>*/List<Channel.Type> getAllowedChannelTypes() {
        return data.channelTypes().toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .map(Channel.Type::of)
                .collect(Collectors.toList());
    }

    /**
     * Whether this option supports auto-complete or not. Default is false.
     * </p>
     * Autocomplete cannot be enabled on options that have choices.
     * @return Whether this option supports auto-complete or not.
     */
    public boolean hasAutocompleteEnabled() {
        return data.autocomplete().toOptional().orElse(false);
    }

    /**
     * Returns the minimum value a user is allowed to input, represented as a {@link Double}.
     * </p>
     * This is only applicable to {@link Type#INTEGER} and {@link Type#NUMBER} types.
     * @return The minimum value a user is allowed to input if present, otherwise {@link Optional#empty()}.
     */
    public Optional<Double> getMinimumValue() {
        return data.minValue().toOptional();
    }

    /**
     * Returns the maximum value a user is allowed to input, represented as a {@link Double}.
     * </p>
     * This is only applicable to {@link Type#INTEGER} and {@link Type#NUMBER} types.
     * @return The maximum value a user is allowed to input if present, otherwise {@link Optional#empty()}.
     */
    public Optional<Double> getMaximumValue() {
        return data.maxValue().toOptional();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommandoptiontype">
     * Application Command Option Type</a>
     */
    public enum Type {
        UNKNOWN(-1),
        SUB_COMMAND(1),
        SUB_COMMAND_GROUP(2),
        STRING(3),
        INTEGER(4),
        BOOLEAN(5),
        USER(6),
        CHANNEL(7),
        ROLE(8),
        MENTIONABLE(9),
        NUMBER(10),
        ATTACHMENT(11);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs an {@code ApplicationCommandOptionType}.
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
         * Gets the type of an application command option. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of option.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1: return SUB_COMMAND;
                case 2: return SUB_COMMAND_GROUP;
                case 3: return STRING;
                case 4: return INTEGER;
                case 5: return BOOLEAN;
                case 6: return USER;
                case 7: return CHANNEL;
                case 8: return ROLE;
                case 9: return MENTIONABLE;
                case 10: return NUMBER;
                case 11: return ATTACHMENT;
                default: return UNKNOWN;
            }
        }
    }
}
