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

import discord4j.discordjson.json.gateway.PayloadData;
import discord4j.gateway.json.GatewayPayload;
import reactor.core.publisher.Mono;

/**
 * Handler for a gateway payload.
 *
 * @param <T> the type of the payload data
 */
@FunctionalInterface
public interface PayloadHandler<T extends PayloadData> {

    /**
     * Perform an action on a payload together with its context, which allows access to various gateway resources.
     *
     * @param context the payload's gateway context
     */
    Mono<Void> handle(GatewayPayload<T> context);
}
