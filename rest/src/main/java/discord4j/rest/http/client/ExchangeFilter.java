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
