package discord4j.rest.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;

public class EmptyWriterStrategy implements WriterStrategy<Void> {

	@Override
	public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
		return type == null;
	}

	@Override
	public Mono<Void> write(HttpClientRequest request, @Nullable Void body) {
		return request.send();
	}
}
