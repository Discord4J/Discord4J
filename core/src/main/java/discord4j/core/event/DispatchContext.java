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

/**
 * Represents gateway dispatch data enriched with context for processing through a
 * {@link DispatchHandler} defined under {@link DispatchHandlers}
 *
 * @param <D> the type of the {@link discord4j.common.json.payload.dispatch.Dispatch} payload
 */
public class DispatchContext<D extends Dispatch> {

	private final D dispatch;
	private final Object client; // TODO use actual Client reference

	public static <D extends Dispatch> DispatchContext<D> of(D dispatch, Object client) {
		return new DispatchContext<>(dispatch, client);
	}

	private DispatchContext(D dispatch, Object client) {
		this.dispatch = dispatch;
		this.client = client;
	}

	public D getDispatch() {
		return dispatch;
	}

	public Object getClient() {
		return client;
	}
}
