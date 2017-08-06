/**
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package discord4j.http;

import discord4j.http.entity.DatadogPayload;
import discord4j.http.function.BodyInserters;
import discord4j.http.function.client.WebClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public class HttpBinTest {

    private static final Logger log = LoggerFactory.getLogger(HttpBinTest.class);

    private final WebClient client = WebClient.create("https://httpbin.org");

    @Test
    public void ipTest() {
        String result = client.get()
                .uri("/ip")
                .exchange()
                .flatMap(response -> response.bodyToMono(String.class))
                .toFuture()
                .join();
        log.info("/ip: {}", result);
    }

    @Test
    public void getTest() {
        String result = client.get()
                .uri("/get")
                .exchange()
                .flatMap(response -> response.bodyToMono(String.class))
                .toFuture()
                .join();
        log.info("/get: {}", result);
    }

    @Test
    public void postTest() {
        DatadogPayload payload = new DatadogPayload();
        payload.setTitle("Running a test");
        payload.setBody("From webflux! @ " + Instant.now());

        String result = client.post()
                .uri("/post")
                .contentType("application/json")
                .contentLength(0) // httpbin requires content-length header
                .body(BodyInserters.fromObject(payload))
                .exchange()
                .flatMap(response -> response.bodyToMono(String.class))
                .toFuture()
                .join();
        log.info("/post: {}", result);
    }
}
