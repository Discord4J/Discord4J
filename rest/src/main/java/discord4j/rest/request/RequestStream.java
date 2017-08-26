package discord4j.rest.request;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

public class RequestStream<T> {

	private final EmitterProcessor<DiscordRequest<T>> backing = EmitterProcessor.create(false);

	void push(DiscordRequest<T> request) {
		backing.onNext(request);
	}

	Mono<DiscordRequest<T>> read() {
		return backing.next();
	}
}
