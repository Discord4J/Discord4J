package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class EmptyWriterStrategy implements WriterStrategy<Void> {

	@Override
	public boolean canWrite(@Nullable Type type, @Nullable String contentType) {
		return type == null;
	}

	@Override
	public Mono<Void> write(HttpClientRequest request, Void body) {
		return request.send();
	}
}
