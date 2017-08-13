package discord4j.route;

import discord4j.http.client.ExchangeFilter;
import discord4j.http.client.SimpleHttpClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

public class SimpleRouter implements Router {

	private final SimpleHttpClient client;

	public SimpleRouter(SimpleHttpClient client) {
		this.client = client;
	}

	@Override
	public <R> Mono<R> exchange(CompleteRoute<R> route) {
		return exchange(route, null);
	}

	@Override
	public <T, R> Mono<R> exchange(CompleteRoute<R> route, @Nullable T requestEntity) {
		return exchange(route, requestEntity, null);
	}

	@Override
	public <T, R> Mono<R> exchange(CompleteRoute<R> route, @Nullable T requestEntity,
	                               @Nullable ExchangeFilter exchangeFilter) {
		return client.exchange(route.getMethod(), route.getUri(), requestEntity, route.getResponseType(),
				exchangeFilter);
	}
}
