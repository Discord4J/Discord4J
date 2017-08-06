package discord4j.route;

import discord4j.http.function.BodyInserter;
import discord4j.http.function.BodyInserters;
import discord4j.http.function.client.ClientResponse;
import discord4j.http.function.client.WebClient;
import discord4j.http.function.client.reactive.ClientHttpRequest;
import discord4j.util.MultiValueMap;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
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

        BodyInserter<?, ClientHttpRequest> bodyInserter = requestEntity == null ? BodyInserters.empty() : BodyInserters.fromObject(requestEntity);
        return exchangeInternal(route, bodyInserter, context);
    }

    @Override
    public <T, R> Mono<R> exchangeForm(Route<R> route, Consumer<HttpClientRequest.Form> formConsumer, @Nullable Context context) {
        Objects.requireNonNull(route);
        Objects.requireNonNull(formConsumer);
        return exchangeInternal(route, BodyInserters.fromMultipartForm(formConsumer), context);
    }

    private <Res> Mono<Res> exchangeInternal(Route<Res> route, BodyInserter<?, ClientHttpRequest> bodyInserter, Context context) {
        HttpMethod method = route.getMethod();
        String uri = route.getUri();

        Function<ClientResponse, Mono<Res>> responseFunction = response -> response.bodyToMono(route.getResponseType());

        Consumer<HttpHeaders> headersConsumer;
        MultiValueMap<String, String> queryParams;
        Object[] uriVariables;
        if (context != null) {
            headersConsumer = context.getHeadersConsumer();
            queryParams = new MultiValueMap<>(context.getQueryParams());
            uriVariables = context.getUriVariables();
        } else {
            headersConsumer = headers -> {
            };
            queryParams = new MultiValueMap<>();
            uriVariables = new Object[0];
        }

        return client.method(method)
                .uri(uriBuilder -> uriBuilder.path(uri)
                        .queryParams(queryParams)
                        .build(uriVariables))
                .headers(headersConsumer)
                .body(bodyInserter)
                .exchange()
                .flatMap(responseFunction);
    }
}
