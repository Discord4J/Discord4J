package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public class FallbackReaderStrategy implements ReaderStrategy<String> {

	@Override
	public boolean canRead(@Nullable Type type, @Nullable String contentType) {
		return true;
	}

	@Override
	public Mono<String> read(HttpClientResponse response, Class<String> responseType) {
		Objects.requireNonNull(response);
		return response.receive().aggregate().asString();
	}
}
