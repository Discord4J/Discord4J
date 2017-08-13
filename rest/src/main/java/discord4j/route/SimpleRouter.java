package discord4j.route;

import discord4j.http.client.SimpleHttpClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Map;

public class SimpleRouter implements Router {

    private final SimpleHttpClient client;

    public SimpleRouter(SimpleHttpClient client) {
        this.client = client;
    }

    @Override
    public <R> Mono<R> exchange(Route<R> route) {
        return exchange(route, null, null);
    }

    @Override
    public <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity) {
        return exchange(route, requestEntity, null);
    }

    @Override
    public <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity, @Nullable Context context) {
        Route<R> completeRoute = route;
        if (context != null) {
            Map<String, Object> mapParameters = context.getQueryParams();
            Object[] arrayParameters = context.getUriVariables();
            if (mapParameters != null) {
                if (arrayParameters != null) {
                    completeRoute = route.complete(mapParameters, arrayParameters);
                } else {
                    completeRoute = route.complete(mapParameters);
                }
            } else {
                if (arrayParameters != null) {
                    completeRoute = route.complete(arrayParameters);
                }
            }
        }
        return client.exchange(completeRoute.getMethod(), completeRoute.getUri(), requestEntity, completeRoute.getResponseType(),
                context != null ? context.getRequestFilter() : null, context != null ? context.getResponseFilter() : null);
    }
}
