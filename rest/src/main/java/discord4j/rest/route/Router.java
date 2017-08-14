package discord4j.rest.route;

import discord4j.rest.http.client.ExchangeFilter;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

/**
 * Provides a abstraction between route declaration and its execution with Reactor Netty-based clients.
 *
 * @since 3.0
 */
public interface Router {

	/**
	 * Execute a given {@link discord4j.rest.route.CompleteRoute}.
	 *
	 * @param route the route to use
	 * @param <R> the type of the response entity
	 * @return a {@link reactor.core.publisher.Mono} that will emit the response entity when the route completes its
	 * execution.
	 */
	<R> Mono<R> exchange(CompleteRoute<R> route);

	/**
	 * Execute a given {@link discord4j.rest.route.CompleteRoute} with a request entity as body.
	 *
	 * @param route the route to use
	 * @param requestEntity the request entity for request body
	 * @param <T> the type of the request entity
	 * @param <R> the type of the response entity
	 * @return a {@link Mono} that will emit the response entity when the route completes its execution.
	 */
	<T, R> Mono<R> exchange(CompleteRoute<R> route, @Nullable T requestEntity);

	/**
	 * Execute a given {@link discord4j.rest.route.CompleteRoute} with a request entity as body and an exchange filter.
	 *
	 * @param route the route to use
	 * @param requestEntity the request entity for request body
	 * @param exchangeFilter the filter to apply to the request and response
	 * @param <T> the type of the request entity
	 * @param <R> the type of the response entity
	 * @return a {@link Mono} that will emit the response entity when the route completes its execution.
	 */
	<T, R> Mono<R> exchange(CompleteRoute<R> route, @Nullable T requestEntity, @Nullable ExchangeFilter
			exchangeFilter);
}
