package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.route.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger log = LoggerFactory.getLogger(StreamPuller.class);

	public <T> void subscribeTo(RequestStream<T> stream) {
		stream.subscribe(req -> {
			httpClient.exchange(req.getMethod(), req.getUri(), req.getBody(), req.getResponseType(), null)
					.doOnError(t -> log.warn("", t))
					.subscribe(response -> {
						if (req.sink != null) {
							req.sink.success(response);
						}
					});
		});
	}
}
