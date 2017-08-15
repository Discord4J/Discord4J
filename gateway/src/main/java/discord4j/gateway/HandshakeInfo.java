/**
 * Copyright 2002-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package discord4j.gateway;

import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple container of information related to the handshake request that started the WebSocketSession session.
 *
 * @author Rossen Stoyanchev
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class HandshakeInfo {

	private final URI uri;

	private final Mono<Principal> principalMono;

	private final HttpHeaders headers;

	private final Optional<String> protocol;


	/**
	 * Constructor with information about the handshake.
	 *
	 * @param uri the endpoint URL
	 * @param headers request headers for server or response headers or client
	 * @param principal the principal for the session
	 * @param protocol the negotiated sub-protocol
	 */
	public HandshakeInfo(URI uri, HttpHeaders headers, Mono<Principal> principal, Optional<String> protocol) {
		Objects.requireNonNull(uri, "URI is required.");
		Objects.requireNonNull(headers, "HttpHeaders are required.");
		Objects.requireNonNull(principal, "Principal is required.");
		Objects.requireNonNull(protocol, "Sub-protocol is required.");
		this.uri = uri;
		this.headers = headers;
		this.principalMono = principal;
		this.protocol = protocol;
	}


	/**
	 * Return the URL for the WebSocket endpoint.
	 */
	public URI getUri() {
		return this.uri;
	}

	/**
	 * Return the handshake HTTP headers. Those are the request headers for a server session and the response headers
	 * for a client session.
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Return the principal associated with the handshake HTTP request.
	 */
	public Mono<Principal> getPrincipal() {
		return this.principalMono;
	}

	/**
	 * The sub-protocol negotiated at handshake time.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6455#section-1.9"> https://tools.ietf
	 * .org/html/rfc6455#section-1.9</a>
	 */
	public Optional<String> getSubProtocol() {
		return this.protocol;
	}


	@Override
	public String toString() {
		return "HandshakeInfo[uri=" + this.uri + ", headers=" + this.headers + "]";
	}

}
