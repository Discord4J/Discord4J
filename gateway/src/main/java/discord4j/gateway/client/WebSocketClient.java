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
import discord4j.gateway.buffer.NettyDataBufferFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.ipc.netty.http.client.HttpClientOptions;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * WebSocketClient implementation for use with Reactor Netty.
 *
 * @author Rossen Stoyanchev
 */
public class WebSocketClient {

	private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

	private static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

	private final HttpClient httpClient;


	/**
	 * Default constructor.
	 */
	public WebSocketClient() {
		this(options -> {
		});
	}

	/**
	 * Constructor that accepts an {@link reactor.ipc.netty.http.client.HttpClientOptions} consumer to supply to {@link
	 * reactor.ipc.netty.http.client.HttpClient#create(java.util.function.Consumer)}.
	 */
	public WebSocketClient(Consumer<? super HttpClientOptions.Builder> clientOptions) {
		this.httpClient = HttpClient.create(clientOptions);
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
		return new HandshakeInfo(url, responseHeaders, Mono.empty(), Optional.ofNullable(protocol));
	}


	/**
	 * Return the configured {@link reactor.ipc.netty.http.client.HttpClient}.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}


	public Mono<Void> execute(URI url, WebSocketHandler handler) {
		return execute(url, new DefaultHttpHeaders(), handler);
	}

	public Mono<Void> execute(URI url, HttpHeaders headers, WebSocketHandler handler) {

		String[] protocols = beforeHandshake(url, headers, handler);

		return getHttpClient()
				.ws(url.toString(),
						nettyHeaders -> {
						},
						Arrays.stream(protocols).collect(Collectors.joining(",")))
				.flatMap(response -> {
					HandshakeInfo info = afterHandshake(url, response.responseHeaders());
					ByteBufAllocator allocator = response.channel().alloc();
					NettyDataBufferFactory factory = new NettyDataBufferFactory(allocator);
					return response.receiveWebsocket((in, out) -> {
						WebSocketSession session = new WebSocketSession(in, out, info, factory);
						return handler.handle(session);
					});
				});
	}

}
