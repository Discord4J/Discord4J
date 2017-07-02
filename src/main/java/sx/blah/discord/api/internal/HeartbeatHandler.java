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
import sx.blah.discord.util.LogMarkers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class HeartbeatHandler {

	private final DiscordWS ws;

	private final int maxMissedPings;
	private final AtomicInteger missedPings = new AtomicInteger(0);
	private final AtomicBoolean waitingForAck = new AtomicBoolean(false);

	private ScheduledExecutorService keepAlive = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Keep-Alive Handler"));
	private final Runnable heartbeatTask;

	private long sentHeartbeatAt;
	private long ackResponseTime;

	HeartbeatHandler(DiscordWS ws, int maxMissedPings) {
		this.ws = ws;
		this.maxMissedPings = maxMissedPings;

		heartbeatTask = () -> {
			if (waitingForAck.get()) { // Missed ping
				missedPings.set(missedPings.get() + 1);
				Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Last heartbeat not acknowledged by Discord. Total: {}", missedPings.get());

				if (missedPings.get() == maxMissedPings) {
					Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Missed max number of heartbeat acks. Opening new session.");
					ws.state = DiscordWS.State.RECONNECTING;
					ws.client.reconnectManager.scheduleReconnect(ws);
				}
			} else {
				missedPings.set(0);
			}

			Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Sending heartbeat on shard {}", ws.shard.getInfo()[0]);
			ws.send(GatewayOps.HEARTBEAT, ws.seq);
			sentHeartbeatAt = System.currentTimeMillis();
			waitingForAck.set(true);
		};
	}

	void begin(long interval) {
		if (keepAlive.isShutdown()) keepAlive = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("Keep-Alive Handler"));

		keepAlive.scheduleAtFixedRate(heartbeatTask, 0, interval, TimeUnit.MILLISECONDS);
	}

	void ack() {
		if (!waitingForAck.get()) {
			Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Received heartbeat ack without sending a heartbeat. Is the websocket out of sync?");
		}
		ackResponseTime = System.currentTimeMillis() - sentHeartbeatAt;
		waitingForAck.set(false);
	}

	void shutdown() {
		missedPings.set(0);
		waitingForAck.set(false);
		keepAlive.shutdown();
	}

	long getAckResponseTime() {
		return ackResponseTime;
	}
}
