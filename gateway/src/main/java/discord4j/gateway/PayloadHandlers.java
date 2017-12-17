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

import discord4j.common.jackson.Possible;
import discord4j.common.json.payload.*;
import discord4j.common.json.payload.dispatch.Dispatch;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;

public class PayloadHandlers {

	private static final Logger log = Loggers.getLogger(PayloadHandlers.class);

	public static void handleDispatch(PayloadContext<Dispatch> ctx) {
		ctx.getClient().dispatch.onNext(ctx.getData());
	}

	public static void handleHeartbeat(PayloadContext<Heartbeat> ctx) {
		// TODO
	}

	public static void handleReconnect(PayloadContext<?> ctx) {
		// TODO
	}

	public static void handleInvalidSession(PayloadContext<InvalidSession> ctx) {
		// TODO
	}

	public static void handleHello(PayloadContext<Hello> ctx) {
		Duration interval = Duration.ofMillis(ctx.getData().getHeartbeatInterval());
		ctx.getClient().heartbeat.start(interval);

		// log trace

		IdentifyProperties props = new IdentifyProperties("linux", "disco", "disco");
		Identify identify = new Identify(ctx.getClient().token, props, false, 250, Possible.absent(), Possible.absent());
		GatewayPayload<Identify> response = GatewayPayload.identify(identify);

		// payloadSender.send(response)
		ctx.getHandler().outbound().onNext(response);
	}

	public static void handleHeartbeatAck(PayloadContext<?> ctx) {
		log.debug("Received heartbeat ack.");
	}


}
