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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    /** The maximum amount of choices that can be in an application command option. */
    public static final int MAX_CHOICES = 25;

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
     * Gets the channel types for what this option is designed.
     *
     * @return a EnumSet with all the {@link Channel.Type} in the class.
     */
    public EnumSet<Channel. Type> getChannelTypes() {
        EnumSet<Channel.Type> presets = EnumSet.noneOf(Channel.Type.class);
        if (data.channelTypes().isAbsent()) {
            return presets;
        }
        presets.addAll(data.channelTypes().toOptional().map(presetValues -> presetValues.stream().map(Channel.Type::of)).orElse(Stream.empty()).collect(Collectors.toList()));
        return presets;
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
     * Gets the Locale and name of the option.
     *
     * @return The locales and names of the option.
     */
    public Map<Locale, String> getLocalizedNames() {
        return data.nameLocalizations().toOptional().orElse(new HashMap<>())
                .entrySet().stream().collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(), Map.Entry::getValue));
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
     * Gets the Locale and description of the option.
     *
     * @return The locales and descriptions of the option.
     */
    public Map<Locale, String> getLocalizedDescriptions() {
        return data.descriptionLocalizations().toOptional().orElse(new HashMap<>())
                .entrySet().stream().collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(), Map.Entry::getValue));
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
     * Gets the options, if the option is a subcommand or subcommand group type.
     *
     * @return The options, if the option is a subcommand or subcommand group type.
     */
    public List<ApplicationCommandOption> getOptions() {
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
     * Gets the minimum number of options that must be chosen for {@link Type#NUMBER} or {@link Type#INTEGER}.
     *
     * @return The minimum number of options that must be chosen.
     */
    public double getMinValues() {
        return data.minValue().toOptional().orElse(1D);
    }

    /**
     * Gets the maximum value allowed for {@link Type#NUMBER} or {@link Type#INTEGER}.
     *
     * @return The maximum value allowed.
     */
    public double getMaxValues() {
        return data.maxValue().toOptional().orElse(Double.MAX_VALUE);
    }

    /**
     * Gets the minimum allowed length for {@link Type#STRING}.
     *
     * @return The minimum allowed length.
     */
    public int getMinLength() {
        return data.minLength().toOptional().orElse(0);
    }

    /**
     * Gets the maximum allowed length for {@link Type#STRING}.
     *
     * @return The maximum allowed length.
     */
    public int getMaxLength() {
        return data.maxLength().toOptional().orElse(Integer.MAX_VALUE);
    }

    /**
     * Gets if autocomplete interactions are enabled for {@link Type#STRING}, {@link Type#INTEGER}, or {@link Type#NUMBER}
     * <br>
     * <b>Note:</b> may not be set to true if {@link #getChoices()} has elements.
     *
     * @return {@code true} if the option has autocomplete, {@code false} otherwise.
     */
    public boolean hasAutocomplete() {
        return data.autocomplete().toOptional().orElse(!getChoices().isEmpty());
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
