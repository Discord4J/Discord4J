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

package discord4j.core.event;

import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.common.json.payload.dispatch.MessageCreate;
import discord4j.common.json.payload.dispatch.Ready;
import discord4j.core.event.domain.*;
import discord4j.gateway.retry.GatewayStateChanged;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link discord4j.common.json.payload.dispatch.Dispatch} to {@link discord4j.core.event.domain.Event}
 * mapping operations.
 */
public abstract class DispatchHandlers {

	private static final Map<Class<?>, DispatchHandler<?, ?>> typeMaps = new ConcurrentHashMap<>(); // or whatever

	static {
		addHandler(Ready.class, DispatchHandlers::handleReady);
		addHandler(MessageCreate.class, DispatchHandlers::handleMessageCreate);
		addHandler(GatewayStateChanged.class, DispatchHandlers::handleGatewayState);
	}

	private static <D extends Dispatch, E extends Event> void addHandler(Class<D> dispatchType, DispatchHandler<D, E>
			dispatchHandler) {
		typeMaps.put(dispatchType, dispatchHandler);
	}

	@SuppressWarnings("unchecked")
	public static <D extends Dispatch, E extends Event> E handle(DispatchContext<D> context) {
		DispatchHandler<D, E> entry = (DispatchHandler<D, E>) typeMaps.get(context.getDispatch().getClass());
		if (entry == null) {
			return null;
		}
		return entry.handle(context);
	}

	private static ReadyEvent handleReady(DispatchContext<Ready> context) {
		return new ReadyEvent(context.getDispatch());
	}

	private static MessageCreatedEvent handleMessageCreate(DispatchContext<MessageCreate> context) {
		return new MessageCreatedEvent(context.getDispatch());
	}

	private static Event handleGatewayState(DispatchContext<GatewayStateChanged> context) {
		GatewayStateChanged dispatch = context.getDispatch();
		switch (dispatch.getState()) {
			case CONNECTED:
				return new ConnectedEvent();
			case RETRY_STARTED:
				return new ReconnectStartedEvent();
			case RETRY_FAILED:
				return new ReconnectFailedEvent();
			case RETRY_SUCCEEDED:
				return new ReconnectedEvent();
			case DISCONNECTED:
				return new DisconnectedEvent();
		}
		return null;
	}

	// and so on ....
}
