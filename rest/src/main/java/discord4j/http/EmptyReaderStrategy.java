package discord4j.http;

import discord4j.pojo.Empty;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class EmptyReaderStrategy implements ReaderStrategy<Void> {

	@Override
	public boolean canRead(@Nullable Type type, @Nullable String contentType) {
		Class<?> rawClass = (Class<?>) type;
		return rawClass != null && (rawClass == Void.class || rawClass == Empty.class);
	}

	@Override
	public Mono<Void> read(HttpClientResponse response, Class<Void> responseType) {
		return Mono.empty();
	}
}
