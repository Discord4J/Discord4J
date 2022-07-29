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
@Experimental
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

    // TODO: Documentation
    public Optional<ApplicationCommandInteractionOptionValue> getValue() {
        return data.value().toOptional()
                .map(value -> new ApplicationCommandInteractionOptionValue(gateway, guildId, data.type(), value));
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
     * Gets the options, present if this option is a group or subcommand.
     *
     * @return The options, present if this option is a group or subcommand.
     */
    public /*~~>*/List<ApplicationCommandInteractionOption> getOptions() {
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
                .filter(data -> data.getName().equals(name))
                .findFirst();
    }

    /**
     * Whether this option is currently focused or not.
     * <p>
     * This will always return false unless this option is from an autocomplete interaction.
     * @return Whether this option is currently focused or not.
     */
    public boolean isFocused() {
        return data.focused().toOptional().orElse(false);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
