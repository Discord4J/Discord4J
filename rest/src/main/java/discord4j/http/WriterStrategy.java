package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public interface WriterStrategy<Req> {

    boolean canWrite(@Nullable Type type, @Nullable String contentType);
    Mono<Void> write(HttpClientRequest request, Req body);
}
