/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;
import sx.blah.discord.util.LogMarkers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the reconnection process for all of the shards of a client. This ensures that shards obey the identify
 * ratelimit when reconnecting.
 */
class ReconnectManager {

	/**
	 * The thread on which {@link DiscordWS#connect()} will be executed.
	 */
	private final ExecutorService reconnectExecutor = Executors.newSingleThreadExecutor(DiscordUtils.createDaemonThreadFactory("Reconnect Thread"));

	/**
	 * Queue of websockets waiting to be reconnected.
	 */
	private final Queue<DiscordWS> websockets = new LinkedList<>();

	/**
	 * The client associated with this manager.
	 */
	private final IDiscordClient client;

	/**
	 * The maximum number of reconnect attempts allowed per {@link DiscordWS} by this manager.
	 */
	private final int maxAttempts;

	/**
	 * The current attempt being executed by the manager. [0, maxAttempts)
	 */
	private int curAttempt;

	/**
	 * If true, {@link #onReconnectError()} may be triggered by {@link DiscordWS#onWebSocketError(Throwable)}
	 * This is used to ensure that {@link #onReconnectError()} is only called once per reconnect attempt.
	 */
	private boolean acknowledgeErrors = true;

	ReconnectManager(IDiscordClient client, int maxAttempts) {
		this.client = client;
		this.maxAttempts = maxAttempts;
	}

	/**
	 * Adds a {@link DiscordWS} to the reconnection queue.
	 * If there is only one WS in the queue, the reconnection process will begin immediately.
	 *
	 * @param ws The websocket to reconnect.
	 */
	synchronized void scheduleReconnect(DiscordWS ws) {
		websockets.offer(ws);
		if (websockets.size() == 1) beginNewReconnect();
	}

	/**
	 * Starts the reconnect process for the next websocket in the queue.
	 *
	 * @throws IllegalStateException if the queue is empty.
	 */
	private void beginNewReconnect() {
		if (websockets.size() == 0) throw new IllegalStateException("Attempt to begin reconnect process with no websockets in queue.");

		Discord4J.LOGGER.info(LogMarkers.RECONNECTS, "Beginning reconnect for shard {}.", websockets.peek().shard.getInfo()[0]);
		performReconnect();
	}

	/**
	 * Calls {@link DiscordWS#connect()} for the current websocket in the queue.
	 */
	private void performReconnect() {
		Discord4J.LOGGER.info(LogMarkers.RECONNECTS, "Performing reconnect attempt {} for shard {}.", curAttempt, websockets.peek().shard.getInfo()[0]);
		reconnectExecutor.submit(() -> {
			acknowledgeErrors = true;
			websockets.peek().connect();
		});
	}

	/**
	 * Called by {@link DiscordWS#onWebSocketText(String)} when it receives {@link GatewayOps#HELLO} which signals that a reconnect was successful.
	 * The current websocket is removed from the queue and the reconnect process for the next one in the queue begins.
	 */
	void onReconnectSuccess() {
		Discord4J.LOGGER.info(LogMarkers.RECONNECTS, "Reconnect for shard {} succeeded.", websockets.peek().shard.getInfo()[0]);
		client.getDispatcher().dispatch(new ReconnectSuccessEvent(websockets.peek().shard));

		acknowledgeErrors = false;
		curAttempt = 0;
		websockets.remove();

		if (websockets.size() > 0) beginNewReconnect();
	}

	/**
	 * Called by {@link DiscordWS#onWebSocketError(Throwable)} for errors which signify that a reconnect attempt failed.
	 * If there are attempts remaining, the the thread is blocked according to {@link #getBackOffMillis()} and the next attempt begins.
	 * If all attempts have been performed, the current {@link DiscordWS} is abandoned and the reconnect process for the next one in the queue begins.
	 */
	void onReconnectError() {
		if (!acknowledgeErrors) return;
		acknowledgeErrors = false;

		reconnectExecutor.submit(() -> {
			client.getDispatcher().dispatch(new ReconnectFailureEvent(websockets.peek().shard, curAttempt, maxAttempts));
			if (curAttempt == maxAttempts - 1) {
				// abandon the ws
				Discord4J.LOGGER.info(LogMarkers.RECONNECTS, "Reconnect for shard {} failed after {} attempts. Abandoning shard.", websockets.peek().shard.getInfo()[0], maxAttempts);
				curAttempt = 0;
				client.getShards().remove(websockets.peek().shard); // remove reference to the shard from the client. It is useless now.
				websockets.remove();

				// begin the next one
				if (websockets.size() > 0) beginNewReconnect();
			} else {
				try {
					long backOff = Math.max(1000, getBackOffMillis());
					Discord4J.LOGGER.debug(LogMarkers.RECONNECTS, "Sleeping for {} ms.", backOff);
					Thread.sleep(backOff);
					curAttempt++;
					performReconnect();
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.RECONNECTS, "Discord4J Internal Exception", e);
				}
			}
		});
	}

	/**
	 * Gets the amount of time the manager should wait before performing the next reconnect attempt depending on {@link #curAttempt}
	 * @return The amount of time to sleep in milliseconds.
	 */
	private long getBackOffMillis() {
		return (2 * curAttempt + ThreadLocalRandom.current().nextInt(0, 3)) * 1000;
	}
}
