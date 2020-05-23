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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.http;

import discord4j.common.json.MessageResponse;
import discord4j.rest.DiscordTest;
import discord4j.rest.RestTests;
import discord4j.rest.json.request.MessageCreateRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RouterTest {

    private static final String channelId = System.getenv("channel");

    private Router router;

    @BeforeAll
    public void setup() {
        router = RestTests.defaultRouter();
    }

    @DiscordTest
    public void test() throws Exception {
        MessageCreateRequest body = new MessageCreateRequest("hello at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router)
                .subscribe(response -> System.out.println("complete response"));

        TimeUnit.SECONDS.sleep(1);
    }

    @DiscordTest
    public void orderingTest() throws Exception {
        String cid = Integer.toHexString(this.hashCode());

        for (int i = 0; i < 10; i++) {
            final int a = i;

            MessageCreateRequest body = new MessageCreateRequest(cid + " " + a, null, false, null);

            Routes.MESSAGE_CREATE.newRequest(channelId)
                    .body(body)
                    .exchange(router)
                    .subscribe(response -> System.out.println("response " + a + ": " + response.getContent()));
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @DiscordTest
    public void testMultiSub() throws Exception {
        MessageCreateRequest body = new MessageCreateRequest("hi", null, false, null);

        Mono<MessageResponse> mono = Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router);

        mono.subscribe();
        mono.subscribe();

        TimeUnit.SECONDS.sleep(2);
    }

    @DiscordTest
    public void testCustomThreadingModel() throws Exception {
        Scheduler thread = Schedulers.single();

        String cid = Integer.toHexString(this.hashCode());

        for (int i = 0; i < 6; i++) {
            final int a = i;

            MessageCreateRequest body = new MessageCreateRequest(cid + " " + a, null, false, null);

            Routes.MESSAGE_CREATE.newRequest(channelId)
                    .body(body)
                    .exchange(router)
                    .publishOn(thread)
                    .cancelOn(thread)
                    .subscribeOn(thread)
                    .subscribe(response -> System.out.println("response " + a + ": " + response.getContent()));
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @DiscordTest
    public void testBlocking() {
        String cid = Integer.toHexString(this.hashCode());

        MessageCreateRequest body0 = new MessageCreateRequest(cid + " 0 at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body0)
                .exchange(router)
                .block();

        MessageCreateRequest body1 = new MessageCreateRequest(cid + " 1 at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body1)
                .exchange(router)
                .block();
    }
}
