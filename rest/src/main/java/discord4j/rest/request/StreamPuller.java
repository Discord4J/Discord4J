package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;

import java.util.Collection;

public class StreamPuller {

	private final SimpleHttpClient httpClient;
	private final Collection<RequestStream<?>> streams;
	private boolean hasStarted = false;

	public StreamPuller(SimpleHttpClient httpClient, Collection<RequestStream<?>> streams) {
		this.httpClient = httpClient;
		this.streams = streams;
	}

	public void start() {
		if (hasStarted) {
			throw new IllegalStateException("Attempt to start already-started StreamPuller");
		}
		streams.forEach(this::subscribeTo);
		hasStarted = true;
	}

	public <T> void subscribeTo(RequestStream<T> stream) {
		stream.subscribe(req -> {
			httpClient.exchange(req.getMethod(), req.getUri(), req.getBody(), req.getResponseType(), null).subscribe(response -> {
				if (req.sink != null) {
					req.sink.success(response);
				}
			});
		});
	}
}
