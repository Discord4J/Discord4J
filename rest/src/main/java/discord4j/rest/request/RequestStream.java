package discord4j.rest.request;

import discord4j.common.util.CircuitBreaker;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class RequestStream<T> {

	private final CircuitBreaker breaker = new CircuitBreaker();
	private final EmitterProcessor<DiscordRequest<T>> backing = EmitterProcessor.create();
	private final Flux<DiscordRequest<T>> stream = backing.zipWith(breaker.getPermitFlux(), 1).map(Tuple2::getT1);

	public Mono<T> push(DiscordRequest<T> request) {
		return Mono.defer(() -> {
			backing.onNext(request);
			return request.mono();
		}).cache();
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
