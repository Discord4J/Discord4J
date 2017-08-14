package discord4j.rest.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;

/**
 * Strategy for encoding an object of type {@code <Req>} and writing the encoded stream of bytes to an {@link
 * reactor.ipc.netty.http.client.HttpClientRequest}.
 *
 * @param <Req> the type of object in the body
 */
public interface WriterStrategy<Req> {

	/**
	 * Whether the given object type is supported by this writer.
	 *
	 * @param type the type of object to check
	 * @param contentType the content type for the write
	 * @return {@code true} if writable, {@code false} otherwise
	 */
	boolean canWrite(@Nullable Class<?> type, @Nullable String contentType);

	/**
	 * Write a given object to the output message.
	 *
	 * @param request the request to write to
	 * @param body the object to write
	 * @return indicates completion or error
	 */
	Mono<Void> write(HttpClientRequest request, @Nullable Req body);
}
