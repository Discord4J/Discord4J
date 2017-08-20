package discord4j.common.util;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class CircuitBreaker {

	private final DirectProcessor<Object> resetNotifier = DirectProcessor.create();
	private volatile boolean isBroken = false;
	private final Flux<Object> permitFlux = Flux.create(sink -> sink.onRequest(l -> {
		if (!isBroken) {
			sink.next(new Object());
		} else {
			resetNotifier.next().subscribe(o -> sink.next(new Object()));
		}
	}));

	public void breakCircuitFor(Duration duration) {
		breakCircuit();
		Mono.delay(duration).subscribe(l -> resetCircuit());
	}

	public void breakCircuit() {
		isBroken = true;
	}

	public void resetCircuit() {
		isBroken = false;
		resetNotifier.onNext(new Object());
	}

	public Flux<Object> getPermitFlux() {
		return permitFlux;
	}
}
