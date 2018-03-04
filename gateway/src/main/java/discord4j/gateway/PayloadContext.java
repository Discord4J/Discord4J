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

import discord4j.common.ResettableInterval;
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.PayloadData;
import discord4j.common.json.payload.dispatch.Dispatch;
import reactor.core.publisher.EmitterProcessor;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents gateway payload data enriched with context for processing through a
 * {@link discord4j.gateway.PayloadHandler} defined under {@link discord4j.gateway.PayloadHandlers}
 *
 * @param <T> the type of the {@link discord4j.common.json.payload.PayloadData}
 */
public class PayloadContext<T extends PayloadData> {

	private final GatewayPayload<T> payload;
	private final EmitterProcessor<Dispatch> dispatch;
	private final EmitterProcessor<GatewayPayload<?>> sender;
	private final AtomicInteger lastSequence;
	private final AtomicReference<String> sessionId;
	private final ResettableInterval heartbeat;
	private final String token;
	private final DiscordWebSocketHandler handler;

	private PayloadContext(GatewayPayload<T> payload, EmitterProcessor<Dispatch> dispatch, EmitterProcessor
			<GatewayPayload<?>> sender, AtomicInteger lastSequence, AtomicReference<String> sessionId,
			ResettableInterval heartbeat, String token, DiscordWebSocketHandler handler) {
		this.payload = payload;
		this.dispatch = dispatch;
		this.sender = sender;
		this.lastSequence = lastSequence;
		this.sessionId = sessionId;
		this.heartbeat = heartbeat;
		this.token = token;
		this.handler = handler;
	}

	public GatewayPayload<T> getPayload() {
		return payload;
	}

	@Nullable
	public T getData() {
		return payload.getData();
	}

	public EmitterProcessor<Dispatch> getDispatch() {
		return dispatch;
	}

	public EmitterProcessor<GatewayPayload<?>> getSender() {
		return sender;
	}

	public AtomicInteger getLastSequence() {
		return lastSequence;
	}

	public AtomicReference<String> getSessionId() {
		return sessionId;
	}

	public ResettableInterval getHeartbeat() {
		return heartbeat;
	}

	public String getToken() {
		return token;
	}

	public DiscordWebSocketHandler getHandler() {
		return handler;
	}

	public static class Builder {

		private GatewayPayload<?> payload;
		private EmitterProcessor<Dispatch> dispatch;
		private EmitterProcessor<GatewayPayload<?>> sender;
		private AtomicInteger lastSequence;
		private AtomicReference<String> sessionId;
		private ResettableInterval heartbeat;
		private String token;
		private DiscordWebSocketHandler handler;

		public Builder setPayload(GatewayPayload<?> payload) {
			this.payload = payload;
			return this;
		}

		public Builder setDispatch(EmitterProcessor<Dispatch> dispatch) {
			this.dispatch = dispatch;
			return this;
		}

		public Builder setSender(EmitterProcessor<GatewayPayload<?>> sender) {
			this.sender = sender;
			return this;
		}

		public Builder setLastSequence(AtomicInteger lastSequence) {
			this.lastSequence = lastSequence;
			return this;
		}

		public Builder setSessionId(AtomicReference<String> sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		public Builder setHeartbeat(ResettableInterval heartbeat) {
			this.heartbeat = heartbeat;
			return this;
		}

		public Builder setToken(String token) {
			this.token = token;
			return this;
		}

		public Builder setHandler(DiscordWebSocketHandler handler) {
			this.handler = handler;
			return this;
		}

		public PayloadContext build() {
			return new PayloadContext<>(payload, dispatch, sender, lastSequence, sessionId, heartbeat, token, handler);
		}
	}
}
