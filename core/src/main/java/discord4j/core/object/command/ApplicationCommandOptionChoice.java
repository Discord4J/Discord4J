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
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.possible.Possible;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Gets the Locale and name of this choice.
     *
     * @return The locales and names of this choice.
     */
    public Map<Locale, String> getLocalizedNames() {
        return Possible.flatOpt(data.nameLocalizations())
                .orElse(Collections.emptyMap())
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(),
                        Map.Entry::getValue));
    }

    /**
     * Gets the value of this choice.
     *
     * @return The value of this choice.
     */
    public Object getValue() {
        return data.value();
    }

    /**
     * Gets the value of this choice as a string.
     *
     * @return The value of this choice as a string.
     */
    public String asString() {
        return String.valueOf(data.value());
    }

    /**
     * Gets the value of this choice as a long.
     *
     * @return The value of this choice as a long.
     */
    public long asLong() {
        try {
            return Long.parseLong(asString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Choice value cannot be converted to long", e);
        }
    }

    /**
     * Gets the value of this choice as a double.
     *
     * @return The value of this choice as a double.
     */
    public double asDouble() {
        try {
            return Double.parseDouble(asString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Choice value cannot be converted to double", e);
        }
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
