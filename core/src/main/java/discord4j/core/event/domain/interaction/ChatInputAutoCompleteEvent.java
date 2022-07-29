/*
 *  This file is part of Discord4J.
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

package discord4j.core.event.domain.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Dispatched when a user is typing an application command option that has auto-complete enabled.
 * <p>
 * You are required to respond to this interaction within a three-second window by using the following:
 * <ul>
 *     <li>{@link #respondWithSuggestions(Iterable)} respond with up to 25 choices to suggest</li>
 * </ul>
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link AutoCompleteInteractionEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class ChatInputAutoCompleteEvent extends AutoCompleteInteractionEvent {

    public ChatInputAutoCompleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /**
     * Requests to respond to the interaction with a list of suggested choices.
     *
     * @param choices The list of suggested choices.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> respondWithSuggestions(Iterable<ApplicationCommandOptionChoiceData> choices) {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
                .choices(choices)
                .build();

        return createInteractionResponse(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT, data);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is an autocomplete interaction.

    /**
     * Gets the ID of the invoked command.
     *
     * @return The ID of the invoked command.
     */
    public Snowflake getCommandId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getId)
                .orElseThrow(IllegalStateException::new); // should always be present for app command auto-complete
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public String getCommandName() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getName)
                .orElseThrow(IllegalStateException::new); // should always be present for app command auto-complete
    }

    /**
     * Gets the type of the invoked command.
     *
     * @return The type of the invoked command.
     */
    public ApplicationCommand.Type getCommandType() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getApplicationCommandType)
                .orElseThrow(IllegalStateException::new); // should always be present for app command auto-complete
    }

    /**
     * Gets the options of the invoked command.
     *
     * @return The options of the invoked command.
     */
    public /*~~>*/List<ApplicationCommandInteractionOption> getOptions() {
        return getInteraction().getCommandInteraction()
                .orElseThrow(IllegalStateException::new) // should always be present for app command auto-complete
                .getOptions();
    }

    /**
     * Gets the option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandInteractionOption> getOption(String name) {
        return getInteraction().getCommandInteraction()
                .orElseThrow(IllegalStateException::new) // should always be present for app command auto-complete
                .getOption(name);
    }

    /**
     * Gets the currently focused option for auto-complete.
     *
     * @return The currently focused option for auto-complete.
     */
    public ApplicationCommandInteractionOption getFocusedOption() {
        return getOptions().stream()
                .map(this::getFocusedOption)
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(Function.identity())
                .orElseThrow(() -> new IllegalStateException("No focused option found"));
    }

    private Optional<ApplicationCommandInteractionOption> getFocusedOption(ApplicationCommandInteractionOption opt) {
        if (opt.isFocused()) {
            return Optional.of(opt); // this option is focused
        }

        return opt.getOptions()
                .stream()
                .map(this::getFocusedOption) // recurse into sub-options
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(Function.identity());
    }
}
