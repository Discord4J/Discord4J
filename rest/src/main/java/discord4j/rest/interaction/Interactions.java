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
import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.RestClient;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static reactor.function.TupleUtils.function;

/**
 * An entry point to build and process Discord interactions. Provides methods to setup an application command handler
 * that can optionally be used to create and update global or guild commands.
 * <p>
 * To begin creating a command you can pass a definition to the
 * {@link #onGuildCommand(ApplicationCommandRequest, Snowflake, Function)} or
 * {@link #onGlobalCommand(ApplicationCommandRequest, Function)} methods. The derived function is a handler that will
 * be run when an interaction for that application command is received. Commands are created or updated by using
 * {@link #createCommands(RestClient)}.
 * <p>
 * Once an {@link RestInteraction} is received, you need to submit a response to Discord within 3 seconds. You can do
 * that
 * by calling one of the {@code acknowledge} or {@code reply} methods under it, that will allow you to work with a
 * followup response handler. Currently this allows you to delete or modify the initial response, while also adding new
 * messages as followup.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands">Slash commands</a>
 */
@Experimental
public class Interactions {

    private static final Logger log = Loggers.getLogger(Interactions.class);

    private final /*~~>*/List<ApplicationCommandDefinition> commands;
    private final /*~~>*/List<ApplicationCommandRequestDefinition> createRequests;

    /**
     * Create a new builder to work with Discord Interactions feature.
     *
     * @return a new Interactions object
     */
    public static Interactions create() {
        return new Interactions();
    }

    Interactions() {
        /*~~>*/this.commands = new CopyOnWriteArrayList<>();
        /*~~>*/this.createRequests = new CopyOnWriteArrayList<>();
    }

    /**
     * Add an application command handler that will match a command by the given Snowflake {@code id}.
     *
     * @param id a command id to match
     * @param action an interaction handler
     * @return this object
     */
    public Interactions onCommand(Snowflake id,
                                  Function<RestInteraction, InteractionHandler> action) {
        commands.add(new RequestApplicationCommandDefinition(acid -> acid.id().equals(Possible.of(id.asString())), action));
        return this;
    }

    /**
     * Add an application command handler that will match a command by the given {@code name}.
     *
     * @param name a command name to match
     * @param action an interaction handler
     * @return this object
     */
    public Interactions onCommand(String name,
                                  Function<RestInteraction, InteractionHandler> action) {
        commands.add(new RequestApplicationCommandDefinition(acid -> acid.name().equals(Possible.of(name)), action));
        return this;
    }

    /**
     * Use an application command definition to also add a handler associated with it. If you call
     * {@link #createCommands(RestClient)} this command will be associated with the given {@code guildId}.
     *
     * @param createRequest a command definition
     * @param guildId a guild ID to supply when creating a command
     * @param action an interaction handler
     * @return this object
     */
    public Interactions onGuildCommand(ApplicationCommandRequest createRequest,
                                       Snowflake guildId,
                                       Function<GuildInteraction, InteractionHandler> action) {
        commands.add(new GuildApplicationCommandDefinition(acid -> acid.name().equals(Possible.of(createRequest.name())), action));
        createRequests.add(new GuildApplicationCommandRequest(createRequest, guildId));
        return this;
    }

    /**
     * Use an application command definition to also add a handler associated with it. If you call
     * {@link #createCommands(RestClient)} this command will be created globally.
     * <p>
     * Global commands can be invoked through a guild or through DMs. You can customize each source behavior using
     * {@link Interactions#createHandler} builder. Leaving a source without a handler will cause an "interaction failed"
     * built-in message as user's reply.
     *
     * @param createRequest a command definition
     * @param action an interaction handler
     * @return this object
     */
    public Interactions onGlobalCommand(ApplicationCommandRequest createRequest,
                                        Function<RestInteraction, InteractionHandler> action) {
        commands.add(new RequestApplicationCommandDefinition(acid -> acid.name().equals(Possible.of(createRequest.name())), action));
        createRequests.add(new GlobalApplicationCommandRequest(createRequest));
        return this;
    }

    /**
     * Send a request upon subscription to create or update all application commands definitions stored in this object.
     *
     * @param restClient the web client used to interact with Discord API
     * @return a {@link Mono} where, upon successful completion, emits nothing, indicating the command have been
     * created or updated. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> createCommands(RestClient restClient) {
        Mono<Long> appIdMono = restClient.getApplicationId();

        return Flux.fromIterable(createRequests)
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

    /**
     * Find the first handler that matches the given {@link InteractionData}.
     *
     * @param interactionData an object containing all information related to a single interaction
     * @return the detected handler, or if found nothing, a handler that does nothing.
     */
    public ApplicationCommandDefinition findHandler(InteractionData interactionData) {
        return interactionData.data().toOptional()
                .flatMap(acid -> commands.stream()
                        .filter(it -> it.test(acid))
                        .findFirst())
                .orElse(NOOP_DEF);
    }

    /**
     * Create a Reactor Netty {@link HttpServer} handler to be applied to a single route using a method like
     * {@link HttpServer#route(Consumer)}. This route will accept interactions from Discord when working in endpoint
     * mode.
     * <p>
     * Currently, no signature validation signature is implemented yet and external measures like a proxy that can
     * validate the incoming requests must be used.
     *
     * @param restClient the web client used to interact with Discord API
     * @return a Reactor Netty server route to handle endpoint-based interactions
     */
    public ReactorNettyServerHandler buildReactorNettyHandler(RestClient restClient) {
        return buildReactorNettyHandler(restClient, new NoopInteractionValidator());
    }

    /**
     * Create a Reactor Netty {@link HttpServer} handler to be applied to a single route using a method like
     * {@link HttpServer#route(Consumer)}. This route will accept interactions from Discord when working in endpoint
     * mode.
     * <p>
     * Currently, no signature validation signature is implemented yet in D4J but you may provide your own
     * implementation of {@link InteractionValidator}
     *
     * @param restClient           the web client used to interact with Discord API
     * @param interactionValidator the validator used to validate incoming requests
     * @return a Reactor Netty server route to handle endpoint-based interactions
     */
    public ReactorNettyServerHandler buildReactorNettyHandler(RestClient restClient, InteractionValidator interactionValidator) {
        return (serverRequest, serverResponse) -> serverRequest.receive()
                .aggregate()
                .asString()
                .flatMap(body -> {
                    String signature = serverRequest.requestHeaders().get("X-Signature-Ed25519");
                    String timestamp = serverRequest.requestHeaders().get("X-Signature-Timestamp");
                    if (interactionValidator.validateSignature(signature, timestamp, body)) {
                        return handleValidated(body, serverResponse, restClient);
                    } else {
                        return serverResponse.status(HttpResponseStatus.UNAUTHORIZED).send();
                    }
                });
    }

    private Mono<Void> handleValidated(String body, HttpServerResponse serverResponse, RestClient restClient) {
        ObjectMapper mapper = restClient.getRestResources().getJacksonResources().getObjectMapper();
        return Mono.fromCallable(() -> mapper.readValue(body, JsonNode.class))
                .flatMap(node -> {
                    int type = node.get("type").asInt();
                    if (type == 1) {
                        return serverResponse.addHeader("content-type", "application/json")
                                .chunkedTransfer(false)
                                .sendString(Mono.just("{\"type\":1}"))
                                .then();
                    } else if (type == 2) {
                        InteractionData interactionData = mapper.convertValue(node, InteractionData.class);
                        InteractionOperations ops = new InteractionOperations(restClient, interactionData);
                        InteractionHandler handler = findHandler(interactionData).createResponseHandler(ops);

                        Scheduler timedScheduler = restClient.getRestResources().getReactorResources()
                                .getTimerTaskScheduler();
                        Scheduler blockScheduler = restClient.getRestResources().getReactorResources()
                                .getBlockingTaskScheduler();

                        return serverResponse.addHeader("content-type", "application/json")
                                .chunkedTransfer(false)
                                .sendString(Mono.fromCallable(() -> mapper.writeValueAsString(handler.response())))
                                .then()
                                .doFinally(s -> Flux.from(handler.onInteractionResponse(ops))
                                        .take(Duration.ofMinutes(15), timedScheduler)
                                        .subscribeOn(blockScheduler)
                                        .subscribe(null,
                                                e -> log.error("Followup handler error", e),
                                                () -> log.info("Followup handler done")));
                    }
                    return serverResponse.sendNotFound();
                })
                .then();
    }

    /**
     * Create an interaction handler that only accepts guild interactions, giving you access to methods specific to
     * {@link GuildInteraction} instances. To create an interaction handler capable of also handling direct messages,
     * see {@link #createHandler()}.
     *
     * @param handlerFunction a mapper to derive an {@link InteractionHandler} from a {@link GuildInteraction}
     * @return an interaction handling function, to be used in methods like
     * {@link #onGlobalCommand(ApplicationCommandRequest, Function)}
     */
    public static Function<RestInteraction, InteractionHandler> guild(Function<GuildInteraction, InteractionHandler> handlerFunction) {
        return new InteractionHandlerSpec(handlerFunction, it -> NOOP_HANDLER_FUNCTION.apply(it)).build();
    }

    /**
     * Create an interaction handler that only accepts direct message interactions, giving you access to methods
     * specific to {@link DirectInteraction} instances. To create an interaction handler capable of also handling
     * guild messages, see {@link #createHandler()}.
     *
     * @param handlerFunction a mapper to derive an {@link InteractionHandler} from a {@link DirectInteraction}
     * @return an interaction handling function, to be used in methods like
     * {@link #onGlobalCommand(ApplicationCommandRequest, Function)}
     */
    public static Function<RestInteraction, InteractionHandler> direct(Function<DirectInteraction,
            InteractionHandler> handlerFunction) {
        return new InteractionHandlerSpec(it -> NOOP_HANDLER_FUNCTION.apply(it), handlerFunction).build();
    }

    /**
     * Start building a new interaction handling function, in order to combine guild and direct message handling. Finish
     * the mapper function by calling {@link InteractionHandlerSpec#build()}. By default this handler does nothing and
     * methods like {@link InteractionHandlerSpec#guild(Function)} or {@link InteractionHandlerSpec#direct(Function)}
     * need to be called to add behavior.
     *
     * @return a new interaction handler builder
     */
    public static InteractionHandlerSpec createHandler() {
        return new InteractionHandlerSpec(it -> NOOP_HANDLER_FUNCTION.apply(it), it -> NOOP_HANDLER_FUNCTION.apply(it));
    }

    /**
     * An alias for a Reactor Netty server route.
     */
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

    static class RequestApplicationCommandDefinition implements ApplicationCommandDefinition {

        private final Predicate<ApplicationCommandInteractionData> matcher;
        private final Function<RestInteraction, InteractionHandler> action;

        public RequestApplicationCommandDefinition(Predicate<ApplicationCommandInteractionData> matcher,
                                                   Function<RestInteraction, InteractionHandler> action) {
            this.matcher = matcher;
            this.action = action;
        }

        @Override
        public boolean test(ApplicationCommandInteractionData acid) {
            return matcher.test(acid);
        }

        public InteractionHandler createResponseHandler(RestInteraction interaction) {
            return action.apply(interaction);
        }
    }

    static class GuildApplicationCommandDefinition implements ApplicationCommandDefinition {

        private final Predicate<ApplicationCommandInteractionData> matcher;
        private final Function<GuildInteraction, InteractionHandler> action;

        public GuildApplicationCommandDefinition(Predicate<ApplicationCommandInteractionData> matcher,
                                                 Function<GuildInteraction, InteractionHandler> action) {
            this.matcher = matcher;
            this.action = action;
        }

        @Override
        public boolean test(ApplicationCommandInteractionData acid) {
            return matcher.test(acid);
        }

        public InteractionHandler createResponseHandler(RestInteraction interaction) {
            return action.apply((GuildInteraction) interaction);
        }
    }

    static ApplicationCommandDefinition NOOP_DEF = new ApplicationCommandDefinition() {
        @Override
        public boolean test(ApplicationCommandInteractionData acid) {
            return true;
        }

        @Override
        public InteractionHandler createResponseHandler(RestInteraction interaction) {
            return NOOP_HANDLER_FUNCTION.apply(interaction);
        }
    };

    static Function<RestInteraction, InteractionHandler> NOOP_HANDLER_FUNCTION = it -> new InteractionHandler() {
        @Override
        public InteractionResponseData response() {
            return it.acknowledge().response();
        }

        @Override
        public Publisher<?> onInteractionResponse(InteractionResponse response) {
            return Mono.empty();
        }
    };

    private static class NoopInteractionValidator implements InteractionValidator {

        @Override
        public boolean validateSignature(String body, String signature, String timestamp) {
            return true;
        }
    }
}
