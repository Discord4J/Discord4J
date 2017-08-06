package discord4j.route;

import discord4j.http.SimpleHttpClient;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SimpleRouter implements Router {

    private final SimpleHttpClient client;

    public SimpleRouter(SimpleHttpClient client) {
        this.client = client;
    }

    @Override
    public <R> Mono<R> exchange(Route<R> route) {
        return client.exchange(route.getMethod(), route.getUri(),
                null, null, null, route.getResponseType());
    }

    @Override
    public <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity) {
        return null;
    }

    @Override
    public <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity, @Nullable Context context) {
        return null;
    }

    @Override
    public <T, R> Mono<R> exchangeForm(Route<R> route, Consumer<HttpClientRequest.Form> formConsumer, @Nullable Context context) {
        return null;
    }
}
