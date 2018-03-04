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
package discord4j.gateway;

import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.PayloadData;

import javax.annotation.Nullable;

public class PayloadContext<T extends PayloadData> {

	private final GatewayPayload<T> payload;
	private final GatewayClient client;
	private final DiscordWebSocketHandler handler;

	public static <T extends PayloadData> PayloadContext<T> of(GatewayPayload<T> payload, GatewayClient client, DiscordWebSocketHandler handler) {
		return new PayloadContext<>(payload, client, handler);
	}

	private PayloadContext(GatewayPayload<T> payload, GatewayClient client, DiscordWebSocketHandler handler) {
		this.payload = payload;
		this.client = client;
		this.handler = handler;
	}

	public GatewayPayload<T> getPayload() {
		return payload;
	}

	@Nullable
	public T getData() {
		return payload.getData();
	}

	public GatewayClient getClient() {
		return client;
	}

	public DiscordWebSocketHandler getHandler() {
		return handler;
	}
}
