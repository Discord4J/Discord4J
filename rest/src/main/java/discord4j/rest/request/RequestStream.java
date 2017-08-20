package discord4j.rest.request;

import discord4j.common.util.CircuitBreaker;
import discord4j.rest.route.Route;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;

public class RequestStream<T> {

	private final CircuitBreaker breaker = new CircuitBreaker();
	private final EmitterProcessor<DiscordRequest<T>> backing = EmitterProcessor.create();
	private final Flux<DiscordRequest<T>> stream = backing.zipWith(breaker.getPermitFlux(), 1).map(Tuple2::getT1);
	private final Route<T> route;

	public RequestStream(Route<T> route) {
		this.route = route;
	}

	public Mono<T> push(Function<Route<T>, DiscordRequest<T>> mapper) {
		return Mono.defer(() -> {
			DiscordRequest<T> request = mapper.apply(route);
			backing.onNext(request);
			return request.mono();
		});
	}

	public Flux<DiscordRequest<T>> getStream() {
		return stream;
	}

	public void pause() {
		breaker.breakCircuit();
	}

	public void resume() {
		breaker.resetCircuit();
	}
}
