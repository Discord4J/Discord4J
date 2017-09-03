package discord4j.rest.request;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class GlobalRateLimiter implements Publisher<Void> {

	private volatile boolean isRateLimited;
	private final EmitterProcessor<Void> resetNotifier = EmitterProcessor.create();
	private final Flux<Void> flux = Flux.create(sink -> sink.onRequest(l -> {
		System.out.println("request");
		if (isRateLimited) {
			Mono.when(resetNotifier).doOnSuccess(e -> sink.complete()).subscribe();
		} else {
			System.out.println("complete");
			sink.complete();
		}
	}));

	public void rateLimitFor(Duration duration) {
		isRateLimited = true;
		Mono.delay(duration).subscribe(l -> {
			isRateLimited = false;
			resetNotifier.onComplete();
		});
	}

	@Override
	public void subscribe(Subscriber<? super Void> s) {
		flux.subscribe(s);
	}
}
