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
package discord4j.rest.request;

import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.ClientRequest;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.util.RouteUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.BackoffDelay;
import reactor.retry.Retry;
import reactor.retry.RetryContext;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A stream of {@link discord4j.rest.request.DiscordRequest DiscordRequests}. Any number of items may be
 * {@link #push(reactor.util.function.Tuple2)} written to the stream. However, the
 * {@link discord4j.rest.request.RequestStream.Reader reader} ensures that only one is read at a time. This
 * linearization ensures proper ratelimit handling.
 * <p>
 * The flow of a request through the stream is as follows:
 *
 * <img src="{@docRoot}img/RequestStream_Flow.png">
 *
 * @param <T> The type of items in the stream.
 */
class RequestStream<T> {

    private final EmitterProcessor<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> backing =
            EmitterProcessor.create(false);
    private final DiscordWebClient httpClient;
    private final GlobalRateLimiter globalRateLimiter;
    /**
     * The retry function used for reading and completing HTTP requests. The back off is determined by the ratelimit
     * headers returned by Discord in the event of a 429. If the bot is being globally ratelimited, the back off is
     * applied to the global rate limiter. Otherwise, it is applied only to this stream.
     */
    private final Retry<AtomicLong> retryFactory = Retry.onlyIf(new Predicate<RetryContext<AtomicLong>>() {
        @Override
        public boolean test(RetryContext<AtomicLong> ctx) {
            Throwable exception = ctx.exception();
            if (exception instanceof ClientException) {
                ClientException clientException = (ClientException) exception;
                if (clientException.getStatus().code() == 429) { // TODO: retry on other status codes?
                    boolean global = Boolean.valueOf(clientException.getHeaders().get("X-RateLimit-Global"));
                    long retryAfter = Long.valueOf(clientException.getHeaders().get("Retry-After"));

                    if (global) {
                        globalRateLimiter.rateLimitFor(Duration.ofMillis(retryAfter));
                    } else {
                        ctx.applicationContext().set(retryAfter);
                    }
                    return true;
                }
            }
            return false;
        }
    }).backoff(context -> {
        if (context.applicationContext() == null) {
            return new BackoffDelay(Duration.ZERO);
        }

        long delay = ((AtomicLong) context.applicationContext()).get();
        ((AtomicLong) context.applicationContext()).set(0L);
        return new BackoffDelay(Duration.ofMillis(delay));
    }).withApplicationContext(new AtomicLong());

    RequestStream(DiscordWebClient httpClient, GlobalRateLimiter globalRateLimiter) {
        this.httpClient = httpClient;
        this.globalRateLimiter = globalRateLimiter;
    }

    void push(Tuple2<MonoProcessor<T>, DiscordRequest<T>> request) {
        backing.onNext(request);
    }

    void start() {
        read().subscribe(new Reader());
    }

    private Mono<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> read() {
        return backing.next();
    }

    /**
     * Reads and completes one request from the stream at a time. If a request fails, it is retried according to the
     * {@link #retryFactory retry function}. The reader may wait in between each request if preemptive ratelimiting is
     * necessary according to the response headers.
     *
     * @see #sleepTime
     * @see #rateLimitHandler
     */
    private class Reader implements Consumer<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> {

        private volatile Duration sleepTime = Duration.ZERO;
        private final Consumer<HttpClientResponse> rateLimitHandler = response -> {
            HttpHeaders headers = response.responseHeaders();

            int remaining = headers.getInt("X-RateLimit-Remaining", -1);
            if (remaining == 0) {
                long resetAt = Long.parseLong(headers.get("X-RateLimit-Reset"));
                long discordTime = headers.getTimeMillis("Date") / 1000;

                sleepTime = Duration.ofSeconds(resetAt - discordTime);
            }
        };

        @SuppressWarnings("ConstantConditions")
        @Override
        public void accept(Tuple2<MonoProcessor<T>, DiscordRequest<T>> tuple) {
            MonoProcessor<T> callback = tuple.getT1();
            DiscordRequest<T> req = tuple.getT2();
            ClientRequest request = new ClientRequest(req.getRoute().getMethod(),
                    RouteUtils.expandQuery(req.getCompleteUri(), req.getQueryParams()),
                    Optional.ofNullable(req.getHeaders())
                            .map(map -> map.entrySet().stream()
                                    .reduce((HttpHeaders) new DefaultHttpHeaders(), (headers, entry) -> {
                                        String key = entry.getKey();
                                        entry.getValue().forEach(value -> headers.add(key, value));
                                        return headers;
                                    }, HttpHeaders::add))
                            .orElse(new DefaultHttpHeaders()));
            Class<T> responseType = req.getRoute().getResponseType();

            Mono.when(globalRateLimiter)
                    .materialize()
                    .flatMap(e -> httpClient.exchange(request, req.getBody(), responseType, rateLimitHandler))
                    .retryWhen(retryFactory)
                    .materialize()
                    .subscribe(signal -> {
                        if (signal.isOnSubscribe()) {
                            callback.onSubscribe(signal.getSubscription());
                        } else if (signal.isOnNext()) {
                            callback.onNext(signal.get());
                        } else if (signal.isOnError()) {
                            callback.onError(signal.getThrowable());
                        } else if (signal.isOnComplete()) {
                            callback.onComplete();
                        }

                        Mono.delay(sleepTime).subscribe(l -> {
                            sleepTime = Duration.ZERO;
                            read().subscribe(this);
                        });
                    });
        }
    }
}
