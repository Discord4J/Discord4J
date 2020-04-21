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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.RestTests;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class RouterTest {

    private ObjectMapper getMapper() {
        return new JacksonResources().getObjectMapper();
    }

    @Test
    public void test() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        MessageCreateRequest body = MessageCreateRequest.builder()
            .content("hello at " + Instant.now())
            .build();

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router)
                .mono()
                .subscribe(response -> System.out.println("complete response"));

        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void orderingTest() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        String cid = Integer.toHexString(this.hashCode());

        for (int i = 0; i < 10; i++) {
            final int a = i;

            MessageCreateRequest body = MessageCreateRequest.builder()
                .content(cid + " " + a)
                .build();

            Routes.MESSAGE_CREATE.newRequest(channelId)
                    .body(body)
                    .exchange(router)
                    .bodyToMono(MessageData.class)
                    .subscribe(response -> System.out.println("response " + a + ": " + response.content()));
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void testMultiSub() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        MessageCreateRequest body = MessageCreateRequest.builder()
            .content("hi")
            .build();

        Mono<MessageData> mono = Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router)
                .bodyToMono(MessageData.class);

        mono.subscribe();
        mono.subscribe();

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void testCustomThreadingModel() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);
        Scheduler thread = Schedulers.single();

        String cid = Integer.toHexString(this.hashCode());

        for (int i = 0; i < 6; i++) {
            final int a = i;

            MessageCreateRequest body = MessageCreateRequest.builder()
                .content(cid + " " + a)
                .build();

            Routes.MESSAGE_CREATE.newRequest(channelId)
                    .body(body)
                    .exchange(router)
                    .bodyToMono(MessageData.class)
                    .publishOn(thread)
                    .cancelOn(thread)
                    .subscribeOn(thread)
                    .subscribe(response -> System.out.println("response " + a + ": " + response.content()));
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void testBlocking() {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        String cid = Integer.toHexString(this.hashCode());

        MessageCreateRequest body0 = MessageCreateRequest.builder()
            .content(cid + " 0 at" + Instant.now())
            .build();

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body0)
                .exchange(router)
                .mono()
                .block();

        MessageCreateRequest body1 = MessageCreateRequest.builder()
            .content(cid + " 1 at" + Instant.now())
            .build();

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body1)
                .exchange(router)
                .mono()
                .block();
    }
}
