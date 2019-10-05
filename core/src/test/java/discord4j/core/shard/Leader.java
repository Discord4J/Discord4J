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

package discord4j.core.shard;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.rsocket.transport.netty.server.CloseableChannel;
import reactor.core.publisher.Mono;

public class Leader {

    public static void main(String[] args) {
        CloseableChannel server = RSocketShardCoordinator.startLocalServer(32323).block();
        assert server != null;
        DiscordClient.create(System.getenv("token"))
                .gateway()
                .setShardCoordinator(new RSocketShardCoordinator("localhost", 32323))
                .setShardFilter(shard -> shard.getIndex() % 2 == 0)
                .withConnection(gateway -> {
                    Mono<Void> exitHandler = gateway.on(MessageCreateEvent.class)
                            .filter(event -> event.getMessage().getContent().orElse("").equals("Test 0"))
                            .flatMap(event -> event.getClient().logout())
                            .then();
                    return Mono.when(exitHandler, gateway.onDisconnect());
                })
                .block();
        server.dispose();
    }
}
