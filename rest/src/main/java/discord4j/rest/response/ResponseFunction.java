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

package discord4j.rest.response;

import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.request.Router;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * A transformation function used while processing {@link DiscordRequest} objects.
 * <p>
 * Using {@code ResponseFunction} objects is targeted to supporting {@link Router} implementations that allow enrichment
 * of a response {@link Mono} pipeline, allowing cross-cutting behavior for specialized error handling or retrying under
 * specific conditions like an HTTP status code or a given API request route. Usage beyond this concern is not
 * supported and could interfere with downstream operations.
 */
public interface ResponseFunction {

    /**
     * Transform a {@link Mono} pipeline using the given {@link DiscordRequest} as hint for parameterization of the
     * resulting transformation.
     *
     * @param request the {@code DiscordRequest} used for the targeted {@code Mono} sequence
     * @param <T> the type of the sequence
     * @return a {@link Function} that allows immediately mapping this {@code Mono} into a target {@code Mono} instance
     */
    <T> Function<Mono<T>, Mono<T>> transform(DiscordRequest<T> request);

    /**
     * Transform every HTTP 404 status code into an empty response into an empty sequence, effectively suppressing
     * the {@link ClientException} that would be forwarded otherwise. See {@link #emptyWhenNotFound(RouteMatcher)}
     * for an override that supports applying the transformation to a subset of requests.
     *
     * @return a {@link ResponseFunction} that transforms any HTTP 404 error into an empty sequence
     */
    static EmptyResponseTransformer emptyWhenNotFound() {
        return new EmptyResponseTransformer(RouteMatcher.any(), ClientException.isStatusCode(404));
    }

    /**
     * Transforms HTTP 404 status codes received by the requests matching the given {@link RouteMatcher} into an
     * empty sequence, effectively suppressing the {@link ClientException} that would be forwarded otherwise. See
     * {@link #emptyWhenNotFound()} to apply this transformation across all {@link Router} requests.
     *
     * @param routeMatcher the {@link RouteMatcher} determining whether to match a particular request
     * @return a {@link ResponseFunction} that transforms matching HTTP 404 errors into an empty sequence
     */
    static EmptyResponseTransformer emptyWhenNotFound(RouteMatcher routeMatcher) {
        return new EmptyResponseTransformer(routeMatcher, ClientException.isStatusCode(404));
    }

    /**
     * Transforms the given <strong>error</strong> status codes received by the requests matching the given
     * {@link RouteMatcher}, effectively suppressing the {@link ClientException} that would be forwarded otherwise.
     * <p>
     * Only a subset of HTTP status codes is supported, like all the ones from 400 and 500 series, except for the 429
     * (Too Many Requests) error that is handled upstream.
     *
     * @param routeMatcher the {@link RouteMatcher} determining whether to match a particular request
     * @param codes the list of HTTP status codes to match when applying this transformation
     * @return a {@link ResponseFunction} that transforms matching requests and response statuses into an empty sequence
     */
    static EmptyResponseTransformer emptyOnErrorStatus(RouteMatcher routeMatcher, Integer... codes) {
        return new EmptyResponseTransformer(routeMatcher, ClientException.isStatusCode(codes));
    }

    /**
     * Applies a retry strategy to retry <strong>once</strong> with a fixed backoff of 1 second to the given
     * <strong>error</strong> status codes received by the requests matching the given {@link RouteMatcher},
     * effectively suppressing the {@link ClientException} that would be forwarded otherwise.
     * <p>
     * Only a subset of HTTP status codes is supported, like all the ones from 400 and 500 series, except for the 429
     * (Too Many Requests) error that is handled upstream.
     * <p>
     * Please note that if you specify error codes 502, 503 or 504 you will replace a built-in retry factory that
     * handles Discord service errors using an exponential backoff with jitter strategy.
     *
     * @param codes the list of HTTP status codes to match when applying this transformation
     * @return a {@link ResponseFunction} that transforms matching response statuses into sequence that retries the
     * request once after waiting 1 second.
     */
    static RetryingTransformer retryOnceOnErrorStatus(Integer... codes) {
        return new RetryingTransformer(RouteMatcher.any(),
                Retry.onlyIf(ClientException.isRetryContextStatusCode(codes))
                        .fixedBackoff(Duration.ofSeconds(1))
                        .retryOnce());
    }

    /**
     * Applies a custom retry strategy to the requests matching the given {@link RouteMatcher}, effectively
     * suppressing the {@link ClientException} that would be forwarded otherwise.
     * <p>
     * Care must be taken when applying this transformation while using a long running retry factory, as it may
     * effectively block further requests on the same rate limiting bucket.
     *
     * @param routeMatcher the {@link RouteMatcher} determining whether to match a particular request
     * @param retry the {@link Retry} factory to install while applying this transformation
     * @return a {@link ResponseFunction} that transforms matching response statuses into sequence that retries the
     * request once after waiting 1 second.
     */
    static RetryingTransformer retryWhen(RouteMatcher routeMatcher, Retry<?> retry) {
        return new RetryingTransformer(routeMatcher, retry);
    }
}
