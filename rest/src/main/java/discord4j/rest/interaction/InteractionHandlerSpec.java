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

package discord4j.rest.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.function.Function;

/**
 * An immutable builder for creating an {@link RestInteraction} handler function, capable of handling guild and direct
 * message interactions. Configurable instances can be acquired through {@link Interactions#createHandler()} and are
 * built using {@link #build()}, yielding a {@link Function} that can be used in methods such as
 * {@link Interactions#onGlobalCommand(ApplicationCommandRequest, Function)}.
 */
@Experimental
public final class InteractionHandlerSpec {

    private final Function<GuildInteraction, InteractionHandler> guildInteractionHandlerFunction;
    private final Function<DirectInteraction, InteractionHandler> directInteractionHandlerFunction;

    InteractionHandlerSpec(Function<GuildInteraction, InteractionHandler> guildInteractionHandlerFunction,
                           Function<DirectInteraction, InteractionHandler> directInteractionHandlerFunction) {
        this.guildInteractionHandlerFunction = guildInteractionHandlerFunction;
        this.directInteractionHandlerFunction = directInteractionHandlerFunction;
    }

    /**
     * Set a guild interaction handler function. Any previous guild interaction handling function set will be
     * overridden.
     *
     * @param guildInteractionHandlerFunction a function to derive an {@link InteractionHandler} from a
     * {@link GuildInteraction}
     * @return this spec for chaining, call {@link #build()} to produce a complete handler
     */
    public InteractionHandlerSpec guild(Function<GuildInteraction, InteractionHandler> guildInteractionHandlerFunction) {
        return new InteractionHandlerSpec(guildInteractionHandlerFunction, directInteractionHandlerFunction);
    }

    /**
     * Set a direct message (DM) interaction handler function. Any previous DM interaction handling function set will be
     * overridden.
     *
     * @param directInteractionHandlerFunction a function to derive an {@link InteractionHandler} from a
     * {@link DirectInteraction}
     * @return this spec for chaining, call {@link #build()} to produce a complete handler
     */
    public InteractionHandlerSpec direct(Function<DirectInteraction, InteractionHandler> directInteractionHandlerFunction) {
        return new InteractionHandlerSpec(guildInteractionHandlerFunction, directInteractionHandlerFunction);
    }

    /**
     * Produce an interaction handler function to be used in a method such as
     * {@link Interactions#onGlobalCommand(ApplicationCommandRequest, Function)} and will route to the appropriate
     * guild or direct message interaction handler.
     *
     * @return a mapper to convert an {@link RestInteraction} into an {@link InteractionHandler}
     */
    public Function<RestInteraction, InteractionHandler> build() {
        return interaction -> {
            if (interaction.getData().guildId().isAbsent()) {
                return directInteractionHandlerFunction.apply((DirectInteraction) interaction);
            } else {
                return guildInteractionHandlerFunction.apply((GuildInteraction) interaction);
            }
        };
    }
}
