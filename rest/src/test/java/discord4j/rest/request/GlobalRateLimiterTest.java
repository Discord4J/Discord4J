package discord4j.rest.request;

import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class GlobalRateLimiterTest {

	@Test
	public void testGlobalRateLimiter() {
		GlobalRateLimiter rateLimiter = new GlobalRateLimiter();

		rateLimiter.rateLimitFor(Duration.ofSeconds(2));

		Mono.when(rateLimiter).block();
		System.out.println("1");

		rateLimiter.rateLimitFor(Duration.ofSeconds(2));

		Mono.when(rateLimiter).block();
		System.out.println("2");
	}
}
