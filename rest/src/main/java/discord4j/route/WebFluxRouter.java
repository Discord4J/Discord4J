package discord4j.route;

import discord4j.http.function.BodyInserter;
import discord4j.http.function.BodyInserters;
import discord4j.http.function.client.ClientResponse;
import discord4j.http.function.client.WebClient;
import discord4j.http.function.client.reactive.ClientHttpRequest;
import discord4j.util.MultiValueMap;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link Router} that leverages the WebFlux reactor-netty client adaptation.
 *
 * @since 3.0
 */
public class WebFluxRouter implements Router {

    private final WebClient client;

    public WebFluxRouter(WebClient client) {
        this.client = client;
    }

    @Override
    public <Res> Mono<Res> exchange(Route<Res> route) {
        return exchange(route, null, null);
    }

    @Override
    public <T, Res> Mono<Res> exchange(Route<Res> route, @Nullable T requestEntity) {
        return exchange(route, requestEntity, null);
    }

    @Override
    public <T, Res> Mono<Res> exchange(Route<Res> route, @Nullable T requestEntity, @Nullable Router.Context context) {
        Objects.requireNonNull(route);

        BodyInserter<?, ClientHttpRequest> bodyInserter;
        if (requestEntity == null) {
            if (context != null && context.getFormConsumer() != null) {
                bodyInserter = BodyInserters.fromMultipartForm(context.getFormConsumer());
            } else {
                bodyInserter = BodyInserters.empty();
            }
        } else {
            bodyInserter = BodyInserters.fromObject(requestEntity);
        }
        return exchangeInternal(route, bodyInserter, context);
    }

    private <Res> Mono<Res> exchangeInternal(Route<Res> route, BodyInserter<?, ClientHttpRequest> bodyInserter, Context context) {
        HttpMethod method = route.getMethod();
        String uri = route.getUri();

        Function<ClientResponse, Mono<Res>> responseFunction = response -> response.bodyToMono(route.getResponseType());

        MultiValueMap<String, String> queryParams;
        Object[] uriVariables;
        if (context != null) {
            queryParams = new MultiValueMap<>();
            Optional.ofNullable(context.getQueryParams())
                    .orElseGet(LinkedHashMap::new)
                    .forEach((k, v) -> queryParams.add(k, v.toString()));
            uriVariables = Optional.ofNullable(context.getUriVariables()).orElseGet(() -> new Object[0]);
        } else {
            queryParams = new MultiValueMap<>();
            uriVariables = new Object[0];
        }

        return client.method(method)
                .uri(uriBuilder -> uriBuilder.path(uri)
                        .queryParams(queryParams)
                        .build(uriVariables))
                .body(bodyInserter)
                .exchange()
                .flatMap(responseFunction);
    }
}
