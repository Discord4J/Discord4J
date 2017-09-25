/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.http.client;

import reactor.ipc.netty.http.client.HttpClientRequest;
import reactor.ipc.netty.http.client.HttpClientResponse;

import java.util.function.Consumer;

/**
 * A filter to manipulate "live" {@link reactor.ipc.netty.http.client.HttpClientRequest} and {@link
 * reactor.ipc.netty.http.client.HttpClientResponse}. Allows operations like setting and retrieving headers when
 * exchanging requests.
 *
 * @see SimpleHttpClient#exchange
 */
public class ExchangeFilter {

	private final Consumer<HttpClientRequest> requestFilter;
	private final Consumer<HttpClientResponse> responseFilter;

	private ExchangeFilter(Consumer<HttpClientRequest> requestFilter, Consumer<HttpClientResponse> responseFilter) {
		this.requestFilter = requestFilter;
		this.responseFilter = responseFilter;
	}

	/**
	 * An {@link discord4j.rest.http.client.ExchangeFilter} builder.
	 *
	 * @return a builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A pre-made {@link discord4j.rest.http.client.ExchangeFilter} that will set a <code>Content-Type</code> header
	 * with the given value.
	 *
	 * @param contentType the value for the content type header
	 * @return a pre-made {@link discord4j.rest.http.client.ExchangeFilter}
	 */
	public static ExchangeFilter requestContentType(String contentType) {
		return builder().requestFilter(req -> req.header("Content-Type", contentType)).build();
	}

	/**
	 * Manipulate the request with the given consumer. The request provided to the consumer is "live", so it can be
	 * used
	 * to add or remove headers, for example.
	 *
	 * @return a function that consumes a {@link reactor.ipc.netty.http.client.HttpClientRequest}
	 */
	public Consumer<HttpClientRequest> getRequestFilter() {
		return requestFilter;
	}

	/**
	 * Manipulate the response with the given consumer. The response provided to the consumer is "live", so it can be
	 * used to obtain header values, for example.
	 *
	 * @return a function that consumes a {@link reactor.ipc.netty.http.client.HttpClientResponse}
	 */
	public Consumer<HttpClientResponse> getResponseFilter() {
		return responseFilter;
	}

	/**
	 * A mutable builder for a {@link discord4j.rest.http.client.ExchangeFilter}.
	 */
	public static class Builder {

		private Consumer<HttpClientRequest> requestFilter = req -> {};
		private Consumer<HttpClientResponse> responseFilter = res -> {};

		private Builder() {
		}

		/**
		 * Set the function to manipulate a request.
		 *
		 * @param requestFilter a function that consumes a {@link reactor.ipc.netty.http.client.HttpClientRequest}
		 * @return this builder
		 */
		public Builder requestFilter(Consumer<HttpClientRequest> requestFilter) {
			this.requestFilter = requestFilter;
			return this;
		}


		/**
		 * Set the function to manipulate a response.
		 *
		 * @param responseFilter a function that consumes a {@link reactor.ipc.netty.http.client.HttpClientResponse}
		 * @return this builder
		 */
		public Builder responseFilter(Consumer<HttpClientResponse> responseFilter) {
			this.responseFilter = responseFilter;
			return this;
		}

		/**
		 * Build the {@link discord4j.rest.http.client.ExchangeFilter} instance.
		 *
		 * @return an exchange filter
		 */
		public ExchangeFilter build() {
			return new ExchangeFilter(requestFilter, responseFilter);
		}
	}

}
