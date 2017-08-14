package discord4j.http;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;

/**
 * Strategy for reading from a {@link reactor.ipc.netty.http.client.HttpClientResponse} and decoding the stream of bytes
 * to an Object of type {@code <Res>}.
 *
 * @param <Res> the type of object in the read response
 */
public interface ReaderStrategy<Res> {

	/**
	 * Whether the given object type is supported by this reader.
	 *
	 * @param type the type of object to check
	 * @param contentType the content type for the read
	 * @return {@code true} if readable, {@code false} otherwise
	 */
	boolean canRead(@Nullable Class<?> type, @Nullable String contentType);

	/**
	 * Read from the input message and encode to a single object.
	 *
	 * @param response the response to read from
	 * @param responseType the type of object in the response which must have been previously checked via {@link
	 * #canRead(Class, String)}
	 * @return
	 */
	Mono<Res> read(HttpClientResponse response, Class<Res> responseType);
}
