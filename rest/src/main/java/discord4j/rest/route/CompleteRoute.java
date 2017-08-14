package discord4j.rest.route;

import io.netty.handler.codec.http.HttpMethod;

import javax.annotation.Nullable;

public class CompleteRoute<T> {

	private final HttpMethod method;
	private final String uri;
	private final Class<T> responseType;
	@Nullable
	private final String majorVar;

	public CompleteRoute(HttpMethod method, String uri, Class<T> responseType, int majorVarIndex) {
		this.method = method;
		this.uri = uri;
		this.responseType = responseType;
		if (majorVarIndex != -1) {
			int end = uri.indexOf("/", majorVarIndex);
			if (end == -1) {
				end = uri.length();
			}
			majorVar = uri.substring(majorVarIndex, end);
		} else {
			majorVar = null;
		}
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

	@Nullable
	public String getMajorVar() {
		return majorVar;
	}
}
