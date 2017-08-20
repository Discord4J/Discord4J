package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.route.Route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamPuller {

	private final SimpleHttpClient httpClient;
	private final Map<Route<?>, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

	public StreamPuller(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@SuppressWarnings("unchecked")
	public <T> RequestStream<T> from(Route<T> route) {
		return (RequestStream<T>) streamMap.computeIfAbsent(route, k -> {
			RequestStream<T> stream = new RequestStream<>(route);
			subscribeTo(stream);
			return stream;
		});
	}

	<T> void subscribeTo(RequestStream<T> stream) {
		stream.getStream().subscribe(req -> {
			stream.pause();
			httpClient.exchange(req.getMethod(), req.getUri(), req.getBody(), req.getResponseType()).materialize()
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
						stream.resume();
					});
		});
	}
}
