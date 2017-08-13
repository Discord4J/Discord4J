package discord4j.http.client;

import discord4j.http.ReaderStrategy;
import discord4j.http.WriterStrategy;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.ipc.netty.http.client.HttpClientRequest;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

public class SimpleHttpClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final HttpHeaders headers;
    private final List<WriterStrategy<?>> writerStrategies;
    private final List<ReaderStrategy<?>> readerStrategies;

    public SimpleHttpClient(HttpClient httpClient, String baseUrl, HttpHeaders headers,
                            List<WriterStrategy<?>> writerStrategies, List<ReaderStrategy<?>> readerStrategies) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.headers = headers;
        this.writerStrategies = writerStrategies;
        this.readerStrategies = readerStrategies;
    }

    public static SimpleHttpClient.Builder builder() {
        return new SimpleHttpClientBuilder();
    }

    public <R, T> Mono<T> exchange(HttpMethod method, String uri, @Nullable R body,
                                   @Nullable Consumer<HttpClientRequest> requestFilter,
                                   @Nullable Consumer<HttpClientResponse> responseFilter, Class<T> responseType) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(uri);
        Objects.requireNonNull(responseType);

        return httpClient.request(method, baseUrl + uri,
                request -> {
                    headers.forEach(entry -> request.header(entry.getKey(), entry.getValue()));
                    if (requestFilter != null) {
                        requestFilter.accept(request);
                    }
                    String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                    return writerStrategies.stream()
                            .filter(s -> s.canWrite(body != null ? body.getClass() : null, contentType))
                            .findFirst()
                            .map(SimpleHttpClient::<R>cast)
                            .map(s -> s.write(request, body))
                            .orElseGet(() -> Mono.error(new RuntimeException("No strategies to write this request: " +
                                    body + " - " + contentType)));
                })
                .log("discord4j.http.client", Level.FINE)
                .flatMap(response -> {
                    if (responseFilter != null) {
                        responseFilter.accept(response);
                    }
                    String contentType = response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                    return readerStrategies.stream()
                            .filter(s -> s.canRead(responseType.getClass(), contentType))
                            .findFirst()
                            .map(SimpleHttpClient::<T>cast)
                            .map(s -> s.read(response, responseType))
                            .orElseGet(() -> Mono.error(new RuntimeException("No strategies to read this response: " +
                                    responseType.getClass() + " - " + contentType)));
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> WriterStrategy<T> cast(WriterStrategy<?> strategy) {
        return (WriterStrategy<T>) strategy;
    }

    @SuppressWarnings("unchecked")
    private static <T> ReaderStrategy<T> cast(ReaderStrategy<?> strategy) {
        return (ReaderStrategy<T>) strategy;
    }

//
//    public <T> Mono<T> postMultipart(String uri, Class<T> clazz, Consumer<HttpClientRequest.Form> form) {
//        return httpClient.post(uri, req -> {
//            req.addHeader("Content-Type", "multipart/form-data").addHeader("Authorization", token);
//            req.sendForm(form);
//            return req.then();
//        }).flatMap(mapToPOJO(clazz));
//    }

    public interface Builder {
        Builder baseUrl(String baseUrl);

        Builder defaultHeader(String key, String value);

        Builder writerStrategy(WriterStrategy<?> strategy);

        Builder readerStrategy(ReaderStrategy<?> strategy);

        SimpleHttpClient build();
    }
}
