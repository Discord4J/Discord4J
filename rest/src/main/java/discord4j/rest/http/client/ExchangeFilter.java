package discord4j.rest.http.client;

import reactor.ipc.netty.http.client.HttpClientRequest;
import reactor.ipc.netty.http.client.HttpClientResponse;

import java.util.function.Consumer;

public class ExchangeFilter {

	private final Consumer<HttpClientRequest> requestFilter;
	private final Consumer<HttpClientResponse> responseFilter;

	private ExchangeFilter(Consumer<HttpClientRequest> requestFilter, Consumer<HttpClientResponse> responseFilter) {
		this.requestFilter = requestFilter;
		this.responseFilter = responseFilter;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static ExchangeFilter requestContentType(String contentType) {
		return builder().requestFilter(req -> req.header("Content-Type", contentType)).build();
	}

	public Consumer<HttpClientRequest> getRequestFilter() {
		return requestFilter;
	}

	public Consumer<HttpClientResponse> getResponseFilter() {
		return responseFilter;
	}

	public static class Builder {

		private Consumer<HttpClientRequest> requestFilter = req -> {};
		private Consumer<HttpClientResponse> responseFilter = res -> {};

		private Builder() {
		}

		public Builder requestFilter(Consumer<HttpClientRequest> requestFilter) {
			this.requestFilter = requestFilter;
			return this;
		}

		public Builder responseFilter(Consumer<HttpClientResponse> responseFilter) {
			this.responseFilter = responseFilter;
			return this;
		}

		public ExchangeFilter build() {
			return new ExchangeFilter(requestFilter, responseFilter);
		}
	}

}
