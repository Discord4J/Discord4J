package discord4j.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

public class JacksonReaderStrategy<Res> implements ReaderStrategy<Res> {

	private final ObjectMapper objectMapper;

	public JacksonReaderStrategy(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean canRead(@Nullable Class<?> type, @Nullable String contentType) {
		if (type == null || contentType == null || !contentType.startsWith("application/json")) {
			return false;
		}

		// A Route<String> should be read by the FallbackReader
		return !CharSequence.class.isAssignableFrom(type) && objectMapper.canDeserialize(getJavaType(type));
	}

	@Override
	public Mono<Res> read(HttpClientResponse response, Class<Res> responseType) {
		Objects.requireNonNull(response);
		Objects.requireNonNull(responseType);
		return response.receive().aggregate().asByteArray().map(bytes -> {
			try {
				return objectMapper.readValue(bytes, responseType);
			} catch (IOException e) {
				throw Exceptions.propagate(e);
			}
		});
	}

	private JavaType getJavaType(Type type) {
		return objectMapper.getTypeFactory().constructType(type);
	}
}
