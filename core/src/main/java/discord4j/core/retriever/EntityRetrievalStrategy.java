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
package discord4j.core.retriever;

import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Defines the entity retrieval strategy to use for a given {@link GatewayDiscordClient}.
 * <p>
 * This class pre-defines some factories according to its main modes of operations:
 * <ul>
 *     <li>{@link EntityRetrievalStrategy#STORE} to <strong>exclusively</strong> fetch data from the Gateway state
 *     cache.</li>
 *     <li>{@link EntityRetrievalStrategy#REST} to fetch data directly from the REST API.</li>
 *     <li>{@link EntityRetrievalStrategy#STORE_FALLBACK_REST} to attempt fetching from the state cache, and if not
 *     successful, fetch from REST. This the default mode.</li>
 * </ul>
 */
@FunctionalInterface
public interface EntityRetrievalStrategy extends Function<GatewayDiscordClient, EntityRetriever> {

    /**
     * Strategy that consists of retrieving entities from the Gateway state cache. Avoids making REST API requests in
     * case the object is not present in the cache. If you want to perform actions when a requested entity is
     * missing, use operators such as {@link Mono#switchIfEmpty(Mono)}.
     * <p>
     * Note that using this strategy can have some unintended consequences like being unable to fetch some entities not
     * cached by the gateway, for example, private channels. If that is your use case, locally apply a method like
     * {@link GatewayDiscordClient#withRetrievalStrategy(EntityRetrievalStrategy)} using
     * {@link EntityRetrievalStrategy#REST}.
     */
    EntityRetrievalStrategy STORE = StoreEntityRetriever::new;

    /**
     * Strategy that consists of retrieving entities from REST API.
     */
    EntityRetrievalStrategy REST = RestEntityRetriever::new;

    /**
     * Strategy that consists of retrieving entities from stores first, then hit the REST API if not found.
     */
    EntityRetrievalStrategy STORE_FALLBACK_REST = gateway -> new FallbackEntityRetriever(
            new StoreEntityRetriever(gateway), new RestEntityRetriever(gateway));
}
