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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.MessageResponse;
import discord4j.rest.RestTests;
import discord4j.rest.json.request.MessageCreateRequest;
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
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }

    @Test
    public void test() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        MessageCreateRequest body = new MessageCreateRequest("hello at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router)
                .subscribe(response -> System.out.println("complete response"));


        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void orderingTest() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        for (int i = 0; i < 10; i++) {
            final int a = i;

            MessageCreateRequest body = new MessageCreateRequest("hi " + a, null, false, null);

            Routes.MESSAGE_CREATE.newRequest(channelId)
                    .body(body)
                    .exchange(router)
                    .subscribe(response -> System.out.println("response " + a + ": " + response.getContent()));
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void testMultiSub() throws Exception {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        MessageCreateRequest body = new MessageCreateRequest("hi", null, false, null);

        Mono<MessageResponse> mono = Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body)
                .exchange(router);

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

        for (int i = 0; i < 6; i++) {
            final int a = i;

            MessageCreateRequest body = new MessageCreateRequest("hi " + a, null, false, null);

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

    @Test
    public void testBlocking() {
        String token = System.getenv("token");
        String channelId = System.getenv("channel");

        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        MessageCreateRequest body0 = new MessageCreateRequest("hi 0 at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body0)
                .exchange(router)
                .block();

        MessageCreateRequest body1 = new MessageCreateRequest("hi 1 at" + Instant.now(), null, false, null);

        Routes.MESSAGE_CREATE.newRequest(channelId)
                .body(body1)
                .exchange(router)
                .block();
    }
}
