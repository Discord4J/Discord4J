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

package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.function.Consumer;

public class ExampleBot {

    private static final Logger log = Loggers.getLogger(ExampleBot.class);

    private static String token;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testLogBot() {
        DiscordClient client = new ClientBuilder(token).build();
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(new MessageCreateListener());
        client.login().block();
    }

    private static class MessageCreateListener implements Consumer<MessageCreateEvent> {

        @Override
        public void accept(MessageCreateEvent event) {
            log.info(event.getMessage().getContent().orElse(""));
        }
    }
}
