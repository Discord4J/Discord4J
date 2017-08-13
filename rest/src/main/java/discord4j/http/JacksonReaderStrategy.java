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
	public boolean canRead(@Nullable Type type, @Nullable String contentType) {
		if (contentType == null || !contentType.startsWith("application/json")) {
			return false;
		}
		JavaType javaType = getJavaType(type);
		Class<?> rawClass = javaType.getRawClass();
		return !CharSequence.class.isAssignableFrom(rawClass) && objectMapper.canDeserialize(javaType);
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
		TypeFactory typeFactory = this.objectMapper.getTypeFactory();
		return typeFactory.constructType(type);
	}
}
