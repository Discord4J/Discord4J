package discord4j.rest.route;

import io.netty.handler.codec.http.HttpMethod;

public class CompleteRoute<T> {

	private final HttpMethod method;
	private final String uri;
	private final Class<T> responseType;

	public CompleteRoute(HttpMethod method, String uri, Class<T> responseType) {
		this.method = method;
		this.uri = uri;
		this.responseType = responseType;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public Class<T> getResponseType() {
		return responseType;
	}
}
