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

package discord4j.core.interaction;

import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.rest.interaction.ApplicationCommandHandler;
import discord4j.rest.interaction.InteractionOperations;
import discord4j.rest.interaction.InteractionResponseSource;
import discord4j.rest.interaction.Interactions;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

public class GatewayInteractions extends ReactiveEventAdapter {

    private final Interactions interactions;
    private final MonoProcessor<Long> appId = MonoProcessor.create();

    public GatewayInteractions(Interactions interactions) {
        this.interactions = interactions;
    }

    public static GatewayInteractions create(Interactions interactions) {
        return new GatewayInteractions(interactions);
    }

    @Override
    public Publisher<?> onReady(ReadyEvent event) {
        long applicationId = Snowflake.asLong(event.getData().application().id());
        appId.onNext(applicationId);
        return Mono.empty();
    }

    @Override
    public Publisher<?> onInteractionCreate(InteractionCreateEvent event) {
        ApplicationCommandHandler handler = interactions.findHandler(event.getData());

        InteractionOperations ops = new InteractionOperations(event.getClient().rest(), event.getData(), appId);
        InteractionResponseSource source = handler.createResponseSource(ops);
        InteractionResponseData responseData = source.response();
        long id = Snowflake.asLong(event.getData().id());
        String token = event.getData().token();

        return event.getClient().rest().getInteractionService()
                .createInteractionResponse(id, token, responseData)
                .thenMany(Flux.from(source.followup(ops)));
    }
}
