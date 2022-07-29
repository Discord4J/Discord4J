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
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.component.MessageComponent;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Discord is probably going to rename this. Redo docs then.

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
    public Optional<Snowflake> getId() {
        return data.id().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public Optional<String> getName() {
        return data.name().toOptional();
    }

    /**
     * Gets the type of the invoked command.
     *
     * @return The type of the invoked command.
     */
    public Optional<ApplicationCommand.Type> getApplicationCommandType() {
        return data.type().toOptional().map(ApplicationCommand.Type::of);
    }

    /**
     * Gets the developer-defined custom id of the component.
     *
     * @return The custom id of the component.
     */
    public Optional<String> getCustomId() {
        return data.customId().toOptional();
    }

    /**
     * Gets the type of the component.
     *
     * @return The type of the component.
     */
    public Optional<MessageComponent.Type> getComponentType() {
        return data.componentType().toOptional().map(MessageComponent.Type::of);
    }

    /**
     * Gets the options of the invoked command.
     *
     * @return The options of the invoked command.
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
                .filter(option -> option.getName().equals(name))
                .findFirst();
    }

    /**
     * Gets the values selected if this is a select menu interaction.
     *
     * @return The select menu values selected.
     */
    public Optional</*~~>*/List<String>> getValues() {
        return data.values().toOptional();
    }

    /**
     * Gets the converted users + roles + channels + attachments.
     *
     * @return The converted users + roles + channels + attachments.
     */
    public Optional<ApplicationCommandInteractionResolved> getResolved() {
        return data.resolved().toOptional()
                .map(data -> new ApplicationCommandInteractionResolved(gateway, data, guildId));
    }

    /**
     * Gets the ID of the user or message targeted by a user or message command.
     *
     * @return The id of the user or message targeted.
     */
    public Optional<Snowflake> getTargetId() {
        return data.targetId().toOptional()
                .map(Snowflake::of);
    }

    /**
     * Gets the components of the submitted modal.
     *
     * @return The components of the submitted modal.
     */
    public /*~~>*/List<MessageComponent> getComponents() {
        return data.components().toOptional().orElse(Collections.emptyList()).stream()
                .map(MessageComponent::fromData)
                .collect(Collectors.toList());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "ApplicationCommandInteraction{" +
                "data=" + data +
                ", guildId=" + guildId +
                '}';
    }
}
