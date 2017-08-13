package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class MultipartWriterStrategy implements WriterStrategy<Consumer<HttpClientRequest.Form>> {

	@Override
	public boolean canWrite(@Nullable Type type, @Nullable String contentType) {
		return contentType != null && contentType.equals("multipart/form-data");
	}

	@Override
	public Mono<Void> write(HttpClientRequest request, Consumer<HttpClientRequest.Form> body) {
		return request.chunkedTransfer(false).sendForm(body).then();
	}
}
