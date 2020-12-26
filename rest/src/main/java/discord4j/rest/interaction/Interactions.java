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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.rest.RestClient;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static reactor.function.TupleUtils.function;

public class Interactions {

    private static final Logger log = Loggers.getLogger(Interactions.class);

    private final List<ApplicationCommandHandler> commandHandlers;
    private final List<ApplicationCommandRequestDefinition> commandCreateRequests;

    public static Interactions create() {
        return new Interactions();
    }

    public Interactions() {
        this.commandHandlers = new CopyOnWriteArrayList<>();
        this.commandCreateRequests = new CopyOnWriteArrayList<>();
    }

    public Interactions onCommand(Snowflake command,
                                  Function<Interaction, InteractionHandler> commandHandler) {
        commandHandlers.add(new RequestApplicationCommandHandler(
                acid -> acid.id().equals(command.asString()), commandHandler));
        return this;
    }

    public Interactions onCommand(String name,
                                  Function<Interaction, InteractionHandler> commandHandler) {
        commandHandlers.add(new RequestApplicationCommandHandler(
                acid -> acid.name().equals(name), commandHandler));
        return this;
    }

    public Interactions onGuildCommand(ApplicationCommandRequest createRequest,
                                       Snowflake guild,
                                       Function<Interaction, InteractionHandler> commandHandler) {
        commandHandlers.add(new RequestApplicationCommandHandler(
                acid -> acid.name().equals(createRequest.name()), commandHandler));
        commandCreateRequests.add(new GuildApplicationCommandRequest(createRequest, guild));
        return this;
    }

    public Interactions onGlobalCommand(ApplicationCommandRequest createRequest,
                                        Function<Interaction, InteractionHandler> commandHandler) {
        commandHandlers.add(new RequestApplicationCommandHandler(
                acid -> acid.name().equals(createRequest.name()), commandHandler));
        commandCreateRequests.add(new GlobalApplicationCommandRequest(createRequest));
        return this;
    }

    public Mono<Void> createCommands(RestClient restClient) {
        Mono<Long> appIdMono = restClient.getApplicationId();

        return Flux.fromIterable(commandCreateRequests)
                .zipWith(appIdMono.repeat())
                .flatMap(function((req, appId) -> {
                    if (req instanceof GuildApplicationCommandRequest) {
                        GuildApplicationCommandRequest guildCommandRequest = (GuildApplicationCommandRequest) req;
                        return restClient.getApplicationService()
                                .createGuildApplicationCommand(appId, guildCommandRequest.getGuild(), req.getRequest())
                                .doOnError(e -> log.warn("Unable to create guild command", e))
                                .onErrorResume(e -> Mono.empty());
                    } else {
                        return restClient.getApplicationService()
                                .createGlobalApplicationCommand(appId, req.getRequest())
                                .doOnError(e -> log.warn("Unable to create global command", e))
                                .onErrorResume(e -> Mono.empty());
                    }
                }))
                .then();
    }

    public ApplicationCommandHandler findHandler(InteractionData interactionData) {
        return interactionData.data().toOptional()
                .flatMap(acid -> commandHandlers.stream()
                        .filter(it -> it.test(acid))
                        .findFirst())
                .orElse(NOOP_HANDLER);
    }

    public ReactorNettyServerHandler buildReactorNettyHandler(RestClient restClient) {
        ObjectMapper mapper = restClient.getRestResources().getJacksonResources().getObjectMapper();
        return (serverRequest, serverResponse) -> serverRequest.receive()
                .aggregate()
                .asByteArray()
                .flatMap(buf -> Mono.fromCallable(() -> mapper.readValue(buf, JsonNode.class)))
                .flatMap(node -> {
                    int type = node.get("type").asInt();
                    if (type == 1) {
                        return serverResponse.addHeader("content-type", "application/json")
                                .chunkedTransfer(false)
                                .sendString(Mono.just("{\"type\":1}"))
                                .then();
                    } else if (type == 2) {
                        InteractionData interactionData = mapper.convertValue(node, InteractionData.class);
                        // Fetching the app ID this way works because we only support bots for now
                        Mono<Long> appIdMono = restClient.getApplicationId();

                        ApplicationCommandHandler handler = findHandler(interactionData);

                        InteractionOperations ops = new InteractionOperations(
                                restClient, interactionData, appIdMono);
                        InteractionResponseSource responseSource = handler.createResponseSource(ops);

                        return serverResponse.addHeader("content-type", "application/json")
                                .chunkedTransfer(false)
                                .sendString(Mono.fromCallable(() -> mapper.writeValueAsString(responseSource.response())))
                                .then()
                                .doFinally(s -> Mono.from(responseSource.followup(ops))
                                        .subscribeOn(restClient.getRestResources().getReactorResources()
                                                .getBlockingTaskScheduler())
                                        .subscribe(null,
                                                e -> log.error("Followup handler error", e),
                                                () -> log.info("Followup handler done")));
                    }
                    return serverResponse.sendNotFound();
                })
                .then();
    }

    public interface ReactorNettyServerHandler extends
            BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {
    }

    interface ApplicationCommandRequestDefinition {
        ApplicationCommandRequest getRequest();
    }

    static class GuildApplicationCommandRequest implements ApplicationCommandRequestDefinition {

        private final ApplicationCommandRequest request;
        private final long guild;

        public GuildApplicationCommandRequest(ApplicationCommandRequest request, Snowflake guild) {
            this.request = request;
            this.guild = guild.asLong();
        }

        @Override
        public ApplicationCommandRequest getRequest() {
            return request;
        }

        public long getGuild() {
            return guild;
        }
    }

    static class GlobalApplicationCommandRequest implements ApplicationCommandRequestDefinition {

        private final ApplicationCommandRequest request;

        public GlobalApplicationCommandRequest(ApplicationCommandRequest request) {
            this.request = request;
        }

        @Override
        public ApplicationCommandRequest getRequest() {
            return request;
        }
    }

    static class RequestApplicationCommandHandler implements ApplicationCommandHandler {

        private final Predicate<ApplicationCommandInteractionData> matcher;
        private final Function<Interaction, InteractionHandler> handler;

        public RequestApplicationCommandHandler(Predicate<ApplicationCommandInteractionData> matcher,
                                                Function<Interaction, InteractionHandler> handler) {
            this.matcher = matcher;
            this.handler = handler;
        }

        @Override
        public boolean test(ApplicationCommandInteractionData acid) {
            return matcher.test(acid);
        }

        public InteractionResponseSource createResponseSource(Interaction interaction) {
            return new HandlerInteractionResponseSource(handler.apply(interaction));
        }
    }

    static class HandlerInteractionResponseSource implements InteractionResponseSource {

        private final InteractionHandler handler;

        public HandlerInteractionResponseSource(InteractionHandler handler) {this.handler = handler;}

        @Override
        public InteractionResponseData response() {
            return handler.response();
        }

        @Override
        public Publisher<?> followup(InteractionResponse response) {
            return handler.onInteractionResponse(response);
        }
    }

    protected static ApplicationCommandHandler NOOP_HANDLER = new ApplicationCommandHandler() {
        @Override
        public boolean test(ApplicationCommandInteractionData acid) {
            return true;
        }

        @Override
        public InteractionResponseSource createResponseSource(Interaction interaction) {
            return new InteractionResponseSource() {
                @Override
                public InteractionResponseData response() {
                    return interaction.acknowledge().response();
                }

                @Override
                public Publisher<?> followup(InteractionResponse response) {
                    return Mono.empty();
                }
            };
        }
    };
}
