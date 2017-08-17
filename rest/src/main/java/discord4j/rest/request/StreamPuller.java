package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;

public class StreamPuller {

	private final SimpleHttpClient httpClient;

	public StreamPuller(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
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
