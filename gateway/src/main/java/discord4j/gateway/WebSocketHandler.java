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

import discord4j.gateway.adapter.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * Handler for a WebSocket session.
 *
 * @author Rossen Stoyanchev
 */
public interface WebSocketHandler {

	/**
	 * Return the list of sub-protocols supported by this handler. <p>By default an empty array is returned.
	 */
	default String[] getSubProtocols() {
		return new String[0];
	}

	/**
	 * Handle the WebSocket session.
	 *
	 * @param session the session to handle
	 * @return completion {@code Mono<Void>} to indicate the outcome of the WebSocket session handling.
	 */
	Mono<Void> handle(WebSocketSession session);

}
