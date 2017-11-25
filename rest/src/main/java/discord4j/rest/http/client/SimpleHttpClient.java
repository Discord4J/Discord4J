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

import discord4j.rest.http.ReaderStrategy;
import discord4j.rest.http.WriterStrategy;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * A simple wrapper over <a href="https://github.com/reactor/reactor-netty">Reactor Netty</a> to perform web requests on
 * the client side.
 *
 * @since 3.0
 */
public class SimpleHttpClient {

	private final HttpClient httpClient;
	private final String baseUrl;
	private final HttpHeaders defaultHeaders;
	private final List<WriterStrategy<?>> writerStrategies;
	private final List<ReaderStrategy<?>> readerStrategies;

	SimpleHttpClient(HttpClient httpClient, String baseUrl, HttpHeaders defaultHeaders,
	                 List<WriterStrategy<?>> writerStrategies, List<ReaderStrategy<?>> readerStrategies) {
		this.httpClient = httpClient;
		this.baseUrl = baseUrl;
		this.defaultHeaders = defaultHeaders;
		this.writerStrategies = writerStrategies;
		this.readerStrategies = readerStrategies;
	}

	/**
	 * Obtain a {@link discord4j.rest.http.client.SimpleHttpClient} builder.
	 *
	 * @return a builder
	 */
	public static SimpleHttpClient.Builder builder() {
		return new SimpleHttpClientBuilder();
	}

	@SuppressWarnings("unchecked")
	private static <T> WriterStrategy<T> cast(WriterStrategy<?> strategy) {
		return (WriterStrategy<T>) strategy;
	}

	@SuppressWarnings("unchecked")
	private static <T> ReaderStrategy<T> cast(ReaderStrategy<?> strategy) {
		return (ReaderStrategy<T>) strategy;
	}

	/**
	 * Exchange a request for a {@link reactor.core.publisher.Mono} response of the specified type.
	 * <p>
	 * The request will be processed according to the writer strategies and its response according to the reader
	 * strategies available.
	 * <p>
	 * The raw request and response objects can be manipulated with a given
	 * {@link discord4j.rest.http.client.ExchangeFilter},
	 * for example, to read and/or write to headers.
	 *
	 * @param method the HTTP method
	 * @param uri the URI used in this request. Will be appended to the base URI, if exists
	 * @param body an object representing the body of the request
	 * @param responseType the desired response type
	 * @param exchangeFilter the filter to use while executing this request
	 * @param <R> the type of the request body, can be <code>null</code>
	 * @param <T> the type of the response body, can be {@link java.lang.Void}
	 * @return a {@link reactor.core.publisher.Mono} of {@link T} with the response
	 */
	public <R, T> Mono<T> exchange(HttpMethod method, String uri, @Nullable R body, Class<T> responseType,
	                               ExchangeFilter exchangeFilter) {
		Objects.requireNonNull(method);
		Objects.requireNonNull(uri);
		Objects.requireNonNull(responseType);

		return httpClient.request(method, baseUrl + uri,
				request -> {
					defaultHeaders.forEach(entry -> request.header(entry.getKey(), entry.getValue()));
					exchangeFilter.getRequestFilter().accept(request);

					String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);
					return writerStrategies.stream()
							.filter(s -> s.canWrite(body != null ? body.getClass() : null, contentType))
							.findFirst()
							.map(SimpleHttpClient::<R>cast)
							.map(s -> s.write(request, body))
							.orElseGet(() -> Mono.error(new RuntimeException("No strategies to write this request: " +
									body + " - " + contentType)));
				})
				.log(SimpleHttpClient.class.getName(), Level.FINEST)
				.flatMap(response -> {
					exchangeFilter.getResponseFilter().accept(response);

					int responseStatus = response.status().code();
					if (responseStatus >= 400 && responseStatus < 600) {
						throw new ClientException(response.status(), response.responseHeaders());
					}

					String contentType = response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
					return readerStrategies.stream()
							.filter(s -> s.canRead(responseType, contentType))
							.findFirst()
							.map(SimpleHttpClient::<T>cast)
							.map(s -> s.read(response, responseType))
							.orElseGet(() -> Mono.error(new RuntimeException("No strategies to read this response: " +
									responseType + " - " + contentType)));
				});
	}

	/**
	 * A mutable builder for a {@link discord4j.rest.http.client.SimpleHttpClient}.
	 */
	public interface Builder {

		/**
		 * Configure a base URI for requests performed through the client to avoid repeating the same host, port and
		 * base path.
		 *
		 * @param baseUrl the base URI for all requests
		 * @return this builder
		 */
		Builder baseUrl(String baseUrl);

		/**
		 * Add the given header to all requests that have not added it.
		 *
		 * @param key the header name
		 * @param value the header value
		 * @return this builder
		 */
		Builder defaultHeader(String key, String value);

		/**
		 * Configure the {@link discord4j.rest.http.WriterStrategy} to use. It will be added to the list of strategies.
		 *
		 * @param strategy the writing strategy to add
		 * @return this builder
		 */
		Builder writerStrategy(WriterStrategy<?> strategy);

		/**
		 * Configure the {@link discord4j.rest.http.ReaderStrategy} to use. It will be added to the list of strategies.
		 *
		 * @param strategy the reading strategy to add
		 * @return this builder
		 */
		Builder readerStrategy(ReaderStrategy<?> strategy);

		/**
		 * Build the {@link discord4j.rest.http.client.SimpleHttpClient} instance.
		 *
		 * @return a client
		 */
		SimpleHttpClient build();
	}
}
