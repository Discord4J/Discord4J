package discord4j.rest.request;

import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class RequestStream<T> {

	private final DirectProcessor<DiscordRequest<T>> backing = DirectProcessor.create();

	public Mono<T> push(DiscordRequest<T> request) {
		backing.onNext(request);
		return request.mono();
	}

	public Disposable subscribe(Consumer<DiscordRequest<T>> consumer) {
		return backing.subscribe(consumer);
	}
}
