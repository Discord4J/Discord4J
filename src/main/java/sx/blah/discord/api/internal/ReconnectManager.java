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
import sx.blah.discord.handle.impl.events.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.ReconnectSuccessEvent;
import sx.blah.discord.util.LogMarkers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectManager {

	private final IDiscordClient client;
	private final ConcurrentLinkedQueue<DiscordWS> toReconnect = new ConcurrentLinkedQueue<>();

	private final int maxAttempts;
	private final AtomicInteger curAttempt = new AtomicInteger(0);

	ReconnectManager(IDiscordClient client, int maxAttempts) {
		this.client = client;
		this.maxAttempts = maxAttempts;
	}

	synchronized void scheduleReconnect(DiscordWS ws) {
		Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Reconnect scheduled for shard {}.", ws.shard.getInfo()[0]);
		toReconnect.offer(ws);
		if (toReconnect.size() == 1) { // If this is the only WS in the queue, immediately begin the reconnect process
			beginReconnect();
		}
	}

	synchronized void onReconnectSuccess() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Reconnect for shard {} succeeded.", toReconnect.peek().shard.getInfo()[0]);
		client.getDispatcher().dispatch(new ReconnectSuccessEvent(toReconnect.peek().shard));
		toReconnect.remove();
		curAttempt.set(0);
		if (toReconnect.peek() != null) {
			try {
				Thread.sleep(5000); // Login ratelimit
				beginReconnect(); // Start next reconnect
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
			}
		}
	}

	synchronized void onReconnectError() {
		client.getDispatcher().dispatch(new ReconnectFailureEvent(toReconnect.peek().shard, curAttempt.get(), maxAttempts));
		if (curAttempt.get() <= maxAttempts) {
			try {
				Thread.sleep(getReconnectDelay()); // Sleep for back off
				incrementAttempt();
				doReconnect(); // Attempt again
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
			}
		} else {
			Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Reconnect for shard {} failed after {} attempts.", toReconnect.peek().shard.getInfo()[0], maxAttempts);
			curAttempt.set(0); // Reset curAttempt for next ws
			toReconnect.remove(); // Remove the current ws from the queue. We've given up trying to reconnect it
			if (toReconnect.peek() != null)  {
				beginReconnect(); // Start process for next in queue
			}
		}
	}

	private void beginReconnect() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Beginning reconnect for shard {}.", toReconnect.peek().shard.getInfo()[0]);
		doReconnect(); // Perform reconnect
	}

	private void doReconnect() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Performing reconnect attempt {}.", curAttempt.get());
		toReconnect.peek().connect();
	}

	private long getReconnectDelay() {
		return ((2 * curAttempt.get()) + ThreadLocalRandom.current().nextLong(0, 2)) * 1000;
	}

	private void incrementAttempt() {
		curAttempt.set(curAttempt.get() + 1);
	}
}
