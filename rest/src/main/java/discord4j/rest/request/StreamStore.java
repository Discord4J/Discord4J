package discord4j.rest.request;

import discord4j.rest.route.Route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamStore {

	private final Map<Route<?>, RequestStream<?>> streamMap = new ConcurrentHashMap<>();
	private final StreamPuller streamPuller;

	public StreamStore(StreamPuller streamPuller) {
		this.streamPuller = streamPuller;
	}

	@SuppressWarnings("unchecked")
	public <T> RequestStream<T> getStream(Route<T> route) {
		return (RequestStream<T>) streamMap.computeIfAbsent(route, k -> {
			RequestStream<T> stream = new RequestStream<>(route);
			streamPuller.subscribeTo(stream);
			return stream;
		});
	}

}
