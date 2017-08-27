package discord4j.rest.request;

import discord4j.rest.http.client.ExchangeFilter;
import discord4j.rest.http.client.SimpleHttpClient;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Router {

	private final SimpleHttpClient httpClient;
	private final Map<Bucket, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

	public Router(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public <T> Mono<T> exchange(DiscordRequest<T> request) {
		return Mono.defer(() -> {
			RequestStream<T> stream = getStream(request);
			stream.push(request);
			return request.mono();
		}).cache();
	}

	@SuppressWarnings("unchecked")
	private <T> RequestStream<T> getStream(DiscordRequest<T> request) {
		return (RequestStream<T>) streamMap.computeIfAbsent(request.getBucket(), k -> {
			RequestStream<T> stream = new RequestStream<>();
			stream.read().subscribe(new StreamConsumer<>(stream));
			return stream;
		});
	}

	class StreamConsumer<T> implements Consumer<DiscordRequest<T>> {

		private final RequestStream<T> stream;
		private volatile Duration sleepTime = Duration.ZERO;
		private final ExchangeFilter exchangeFilter = ExchangeFilter.builder()
				.responseFilter(response -> {
					HttpHeaders headers = response.responseHeaders();

					int remaining = headers.getInt("X-RateLimit-Remaining");
					if (remaining == 0) {
						long resetAt = Long.parseLong(headers.get("X-RateLimit-Reset"));
						long discordTime = OffsetDateTime.parse(headers.get("date"), DateTimeFormatter
								.RFC_1123_DATE_TIME).toEpochSecond();
						sleepTime = Duration.ofSeconds(resetAt - discordTime);
					}
				})
				.build();

		StreamConsumer(RequestStream<T> stream) {
			this.stream = stream;
		}

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
							stream.read().subscribe(this);
							sleepTime = Duration.ZERO;
						});
					});
		}
	}
}
