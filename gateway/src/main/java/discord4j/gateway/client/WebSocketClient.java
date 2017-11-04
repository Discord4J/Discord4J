/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway.client;

import discord4j.gateway.HandshakeInfo;
import discord4j.gateway.WebSocketHandler;
import discord4j.gateway.adapter.WebSocketSession;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WebSocketClient implementation for use with Reactor Netty.
 *
 * @author Rossen Stoyanchev
 */
public class WebSocketClient {

	private static final Logger log = Loggers.getLogger(WebSocketClient.class);
	private static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

	private final HttpClient httpClient;


	public WebSocketClient() {
		this(HttpClient.create());
	}

	public WebSocketClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}


	protected String[] beforeHandshake(URI url, HttpHeaders requestHeaders, WebSocketHandler handler) {
		if (log.isDebugEnabled()) {
			log.debug("Executing handshake to " + url);
		}
		return handler.getSubProtocols();
	}

	protected HandshakeInfo afterHandshake(URI url, HttpHeaders responseHeaders) {
		if (log.isDebugEnabled()) {
			log.debug("Handshake response: " + url + ", " + responseHeaders);
		}
		String protocol = responseHeaders.get(SEC_WEBSOCKET_PROTOCOL);
		return new HandshakeInfo(url, responseHeaders, Optional.ofNullable(protocol));
	}


	public Mono<Void> execute(URI url, WebSocketHandler handler) {
		return execute(url, new DefaultHttpHeaders(), handler);
	}

	public Mono<Void> execute(URI url, HttpHeaders headers, WebSocketHandler handler) {

		String[] protocols = beforeHandshake(url, headers, handler);

		return this.httpClient
				.ws(url.toString(),
						nettyHeaders -> {
						},
						Arrays.stream(protocols).collect(Collectors.joining(",")))
				.flatMap(response -> {
					HandshakeInfo info = afterHandshake(url, response.responseHeaders());
					ByteBufAllocator allocator = response.channel().alloc();
					return response.receiveWebsocket((in, out) -> {
						WebSocketSession session = new WebSocketSession(in, out, info, allocator);
						return handler.handle(session);
					});
				});
	}

}
