package discord4j.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;

public class JacksonReaderStrategy<Res> implements ReaderStrategy<Res> {

    private final ObjectMapper objectMapper;

    public JacksonReaderStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canRead(@Nullable Type type, @Nullable String contentType) {
        return contentType != null && contentType.contains("application/json");
    }

    @Override
    public Mono<Res> read(HttpClientResponse response, Class<Res> responseType) {
        return response.receive().aggregate().asByteArray().map(bytes -> {
            try {
                return objectMapper.readValue(bytes, responseType);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }
}
