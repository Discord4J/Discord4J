package discord4j.rest.request;

import discord4j.rest.http.client.ExchangeFilter;
import discord4j.rest.http.client.SimpleHttpClient;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientException;
import reactor.retry.BackoffDelay;
import reactor.retry.Retry;
import reactor.retry.RetryContext;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A stream of items wrapped in {@link discord4j.rest.request.DiscordRequest DiscordRequests}. Any number of items may
 * be {@link #push(DiscordRequest) written} to the stream. However, the
 * {@link discord4j.rest.request.RequestStream.Reader reader} ensures that only one is read at a time. This
 * linearization ensures proper ratelimit handling.
 *
 * <p>
 * The flow of a request through the stream is as follows:
 * <p>
 * <img src="{@docRoot}img/RequestStream_Flow.png">
 *
 * @param <T> The type of items in the stream.
 */
class RequestStream<T> {

	private final EmitterProcessor<DiscordRequest<T>> backing = EmitterProcessor.create(false);
	private final SimpleHttpClient httpClient;
	private final GlobalRateLimiter globalRateLimiter;
	/**
	 * The retry function used for reading and completing HTTP requests. The back off is determined by the ratelimit
	 * headers returned by Discord in the event of a 429. If the bot is being globally ratelimited, the back off is
	 * applied to the global rate limiter. Otherwise, it is applied only to this stream.
	 */
	private final Retry<AtomicLong> RETRY = Retry.onlyIf(new Predicate<RetryContext<AtomicLong>>() {
		@Override
		public boolean test(RetryContext<AtomicLong> ctx) {
			Throwable exception = ctx.exception();
			if (exception instanceof HttpClientException) {
				HttpClientException httpException = (HttpClientException) exception;
				if (httpException.status().code() == 429) {
					boolean global = Boolean.valueOf(httpException.headers().get("X-RateLimit-Global"));
					long retryAfter = Long.valueOf(httpException.headers().get("Retry-After"));

					if (global) {
						globalRateLimiter.rateLimitFor(Duration.ofMillis(retryAfter));
					} else {
						ctx.applicationContext().set(retryAfter);
					}
				}
			}
			return false;
		}
	}).backoff(context -> {
		long delay = ((AtomicLong) context.applicationContext()).get();
		((AtomicLong) context.applicationContext()).set(0L);
		return new BackoffDelay(Duration.ofMillis(delay));
	});

	RequestStream(SimpleHttpClient httpClient, GlobalRateLimiter globalRateLimiter) {
		this.httpClient = httpClient;
		this.globalRateLimiter = globalRateLimiter;
	}

	void push(DiscordRequest<T> request) {
		backing.onNext(request);
	}

	void start() {
		read().subscribe(new Reader());
	}

	private Mono<DiscordRequest<T>> read() {
		return backing.next();
	}

	/**
	 * Reads and completes one request from the stream at a time. If a request fails, it is retried according to the
	 * {@link #RETRY retry function}. The reader may wait in between each request if preemptive ratelimiting is
	 * necessary according to the response headers.
	 *
	 * @see #sleepTime
	 * @see #exchangeFilter
	 */
	private class Reader implements Consumer<DiscordRequest<T>> {

		private volatile Duration sleepTime = Duration.ZERO;
		private final ExchangeFilter exchangeFilter = ExchangeFilter.builder()
				.responseFilter(response -> {
					HttpHeaders headers = response.responseHeaders();

					int remaining = headers.getInt("X-RateLimit-Remaining");
					if (remaining == 0) {
						long resetAt = Long.parseLong(headers.get("X-RateLimit-Reset"));
						long discordTime = OffsetDateTime.parse(headers.get("Date"),
								DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();

						sleepTime = Duration.ofSeconds(resetAt - discordTime);
					}
				})
				.build();

		@SuppressWarnings("ConstantConditions")
		@Override
		public void accept(DiscordRequest<T> req) {
			Mono.when(globalRateLimiter)
					.materialize()
					.flatMap(e -> httpClient.exchange(req.getRoute().getMethod(), req.getCompleteUri(), req.getBody(),
							req.getRoute().getResponseType(), exchangeFilter))
					.retryWhen(RETRY)
					.materialize()
					.subscribe(signal -> {
						if (signal.isOnSubscribe()) {
							req.mono.onSubscribe(signal.getSubscription());
						} else if (signal.isOnNext()) {
							req.mono.onNext(signal.get());
						} else if (signal.isOnError()) {
							req.mono.onError(signal.getThrowable());
						} else if (signal.isOnComplete()) {
							req.mono.onComplete();
						}

						Mono.delay(sleepTime).subscribe(l -> {
							read().subscribe(this);
							sleepTime = Duration.ZERO;
						});
					});
		}
	}
}
