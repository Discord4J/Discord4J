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

public class SimpleHttpClient {

	private final HttpClient httpClient;
	private final String baseUrl;
	private final HttpHeaders headers;
	private final List<WriterStrategy<?>> writerStrategies;
	private final List<ReaderStrategy<?>> readerStrategies;
	@Nullable
	private final ExchangeFilter defaultExchangeFilter;

	public SimpleHttpClient(HttpClient httpClient, String baseUrl, HttpHeaders headers,
	                        List<WriterStrategy<?>> writerStrategies, List<ReaderStrategy<?>> readerStrategies,
	                        @Nullable ExchangeFilter defaultExchangeFilter) {
		this.httpClient = httpClient;
		this.baseUrl = baseUrl;
		this.headers = headers;
		this.writerStrategies = writerStrategies;
		this.readerStrategies = readerStrategies;
		this.defaultExchangeFilter = defaultExchangeFilter;
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
	                               @Nullable ExchangeFilter exchangeFilter) {
		Objects.requireNonNull(method);
		Objects.requireNonNull(uri);
		Objects.requireNonNull(responseType);

		return httpClient.request(method, baseUrl + uri,
				request -> {
					headers.forEach(entry -> request.header(entry.getKey(), entry.getValue()));
					if (defaultExchangeFilter != null) {
						defaultExchangeFilter.getRequestFilter().accept(request);
					}
					if (exchangeFilter != null) {
						exchangeFilter.getRequestFilter().accept(request);
					}

					String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);

					System.out.println(writerStrategies.size());

					return writerStrategies.stream()
							.filter(s -> s.canWrite(body != null ? body.getClass() : null, contentType))
							.findFirst()
							.map(SimpleHttpClient::<R>cast)
							.map(s -> s.write(request, body))
							.orElseGet(() -> Mono.error(new RuntimeException("No strategies to write this request: " +
									body + " - " + contentType)));
				})
				.log("discord4j.rest.http.client", Level.FINE)
				.flatMap(response -> {
					if (defaultExchangeFilter != null) {
						defaultExchangeFilter.getResponseFilter().accept(response);
					}
					if (exchangeFilter != null) {
						exchangeFilter.getResponseFilter().accept(response);
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

	public interface Builder {

		Builder baseUrl(String baseUrl);

		Builder defaultHeader(String key, String value);

		Builder writerStrategy(WriterStrategy<?> strategy);

		Builder readerStrategy(ReaderStrategy<?> strategy);

		Builder exchangeFilter(ExchangeFilter exchangeFilter);

		SimpleHttpClient build();
	}
}
