package discord4j.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public class JacksonWriterStrategy implements WriterStrategy<Object> {

	private final ObjectMapper objectMapper;

	public JacksonWriterStrategy(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean canWrite(@Nullable Type type, @Nullable String contentType) {
		if (type == null) {
			return false;
		}
		Class<?> rawClass = getJavaType(type).getRawClass();
		return (Object.class == rawClass) || !String.class.isAssignableFrom(rawClass) && objectMapper.canSerialize
				(rawClass);
	}

	@Override
	public Mono<Void> write(HttpClientRequest request, Object body) {
		Objects.requireNonNull(request);
		Objects.requireNonNull(body);
		try {
			return request.sendString(Mono.just(objectMapper.writeValueAsString(body))).then();
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private JavaType getJavaType(Type type) {
		TypeFactory typeFactory = this.objectMapper.getTypeFactory();
		return typeFactory.constructType(type);
	}
}
