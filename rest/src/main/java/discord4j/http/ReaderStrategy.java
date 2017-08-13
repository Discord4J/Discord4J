package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public interface ReaderStrategy<Res> {

	boolean canRead(@Nullable Type type, @Nullable String contentType);
	Mono<Res> read(HttpClientResponse response, Class<Res> responseType);
}
