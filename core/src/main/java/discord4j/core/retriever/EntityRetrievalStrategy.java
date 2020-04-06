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

import java.util.function.Function;

/**
 * Defines the entity retrieval strategy to use for a given {@link GatewayDiscordClient}.
 */
@FunctionalInterface
public interface EntityRetrievalStrategy extends Function<GatewayDiscordClient, EntityRetriever> {

    /**
     * Strategy that consists of retrieving entities from stores.
     */
    static final EntityRetrievalStrategy STORE = StoreEntityRetriever::new;

    /**
     * Strategy that consists of retrieving entities from REST API.
     */
    static final EntityRetrievalStrategy REST = RestEntityRetriever::new;

    /**
     * Strategy that consists of retrieving entities from stores first, then hit the REST API if not found.
     */
    static final EntityRetrievalStrategy STORE_FALLBACK_REST = gateway -> new FallbackEntityRetriever(
            new StoreEntityRetriever(gateway), new RestEntityRetriever(gateway));
}
