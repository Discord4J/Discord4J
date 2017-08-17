package discord4j.rest.request;

import discord4j.rest.route.Route;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class RequestStream<T> {

	private final DirectProcessor<DiscordRequest<T>> backing = DirectProcessor.create();
	private final Route<T> route;

	public RequestStream(Route<T> route) {this.route = route;}

	public Mono<T> push(Function<Route<T>, DiscordRequest<T>> mapper) {
		DiscordRequest<T> request = mapper.apply(route);
		backing.onNext(request);
		return request.mono();
	}

	public Disposable subscribe(Consumer<DiscordRequest<T>> consumer) {
		return backing.subscribe(consumer);
	}
}
