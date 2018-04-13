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

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.ApplicationUser;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ExampleBot {

    private static String token;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testLogoutBot() {
        DiscordClient client = new ClientBuilder(token).build();
        Listeners listeners = new Listeners(client);
        listeners.registerListener(new CoreCommandListener());
        listeners.registerListener(context -> {
            EventDispatcher dispatcher = context.client.getEventDispatcher();
            dispatcher.on(ConnectEvent.class).subscribe();
            dispatcher.on(DisconnectEvent.class).subscribe();
            dispatcher.on(ReconnectStartEvent.class).subscribe();
            dispatcher.on(ReconnectEvent.class).subscribe();
            dispatcher.on(ReconnectFailEvent.class).subscribe();
        });
        // latch to avoid blocking main thread on .login()
        //        CountDownLatch latch = new CountDownLatch(1);
        //        client.login()
        //                .doOnTerminate(latch::countDown)
        //                .doOnCancel(latch::countDown)
        //                .subscribe();
        //        latch.await();
        // or just block it
        client.login().block();
    }

    public static class Listeners {

        private final DiscordClient client;

        Listeners(DiscordClient client) {
            this.client = client;
        }

        private void registerListener(Consumer<ListenerContext> listener) {
            listener.accept(new ListenerContext(client));
        }
    }

    public static class ListenerContext {

        private final DiscordClient client;

        ListenerContext(DiscordClient client) {
            this.client = client;
        }

        public DiscordClient getClient() {
            return client;
        }
    }

    private static class CoreCommandListener implements Consumer<ListenerContext> {

        private final AtomicReference<Snowflake> owner = new AtomicReference<>();

        @Override
        public void accept(ListenerContext context) {
            context.client.getEventDispatcher().on(ReadyEvent.class)
                    .next() // get only once and then unsubscribe
                    .flatMap(ready -> context.client.getApplicationUser())
                    .map(ApplicationUser::getOwnerId)
                    .subscribe(owner::set);

            context.client.getEventDispatcher().on(MessageCreateEvent.class)
                    .subscribe(event -> {
                        Message message = event.getMessage();

                        message.getAuthorId()
                                .filter(id -> id.equals(owner.get())) // only accept bot owner messages
                                .flatMap(id -> message.getContent())
                                .ifPresent(content -> {
                                    if ("!logout".equals(content)) {
                                        context.client.logout();
                                    }
                                });
                    });
        }
    }
}
