package discord4j.rest.request;

import discord4j.rest.http.client.ExchangeFilter;
import discord4j.rest.http.client.SimpleHttpClient;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class RequestStream<T> {

	private final EmitterProcessor<DiscordRequest<T>> backing = EmitterProcessor.create(false);
	private final SimpleHttpClient httpClient;

	RequestStream(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
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
			httpClient.exchange(req.getMethod(), req.getUri(), req.getBody(), req.getResponseType(), exchangeFilter)
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
