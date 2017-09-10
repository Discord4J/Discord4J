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

public class SimpleHttpClient {

	private final HttpClient httpClient;
	private final String baseUrl;
	private final HttpHeaders defaultHeaders;
	private final List<WriterStrategy<?>> writerStrategies;
	private final List<ReaderStrategy<?>> readerStrategies;

	public SimpleHttpClient(HttpClient httpClient, String baseUrl, HttpHeaders defaultHeaders,
	                        List<WriterStrategy<?>> writerStrategies, List<ReaderStrategy<?>> readerStrategies) {
		this.httpClient = httpClient;
		this.baseUrl = baseUrl;
		this.defaultHeaders = defaultHeaders;
		this.writerStrategies = writerStrategies;
		this.readerStrategies = readerStrategies;
	}

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
				.flatMap(response -> {
					exchangeFilter.getResponseFilter().accept(response);

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

	public interface Builder {

		Builder baseUrl(String baseUrl);

		Builder defaultHeader(String key, String value);

		Builder writerStrategy(WriterStrategy<?> strategy);

		Builder readerStrategy(ReaderStrategy<?> strategy);

		SimpleHttpClient build();
	}
}
