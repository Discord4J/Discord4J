package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Write to a request from a {@code Consumer<HttpClientRequest.Form>} using reactor-netty's {@link
 * HttpClientRequest#sendForm(java.util.function.Consumer)}.
 *
 * @see HttpClientRequest.Form
 */
public class MultipartWriterStrategy implements WriterStrategy<Consumer<HttpClientRequest.Form>> {

	@Override
	public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
		return contentType != null && contentType.equals("multipart/form-data");
	}

	@Override
	public Mono<Void> write(HttpClientRequest request, @Nullable Consumer<HttpClientRequest.Form> body) {
		return request.chunkedTransfer(false).sendForm(body).then();
	}
}
