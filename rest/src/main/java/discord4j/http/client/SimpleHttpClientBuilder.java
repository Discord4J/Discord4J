package discord4j.http.client;

import discord4j.http.ReaderStrategy;
import discord4j.http.WriterStrategy;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.ipc.netty.http.client.HttpClient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

class SimpleHttpClientBuilder implements SimpleHttpClient.Builder {

    @Nullable
    private String baseUrl;

    private final HttpHeaders headers = new DefaultHttpHeaders();
    private final List<ReaderStrategy<?>> readerStrategies = new ArrayList<>();
    private final List<WriterStrategy<?>> writerStrategies = new ArrayList<>();

    @Override
    public SimpleHttpClient.Builder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public SimpleHttpClient.Builder defaultHeader(String key, String value) {
        headers.add(key, value);
        return this;
    }

    @Override
    public SimpleHttpClient.Builder writerStrategy(WriterStrategy<?> strategy) {
        writerStrategies.add(strategy);
        return this;
    }

    @Override
    public SimpleHttpClient.Builder readerStrategy(ReaderStrategy<?> strategy) {
        readerStrategies.add(strategy);
        return this;
    }

    @Override
    public SimpleHttpClient build() {
        return new SimpleHttpClient(HttpClient.create(), baseUrl, headers, writerStrategies, readerStrategies);
    }
}
