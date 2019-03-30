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

import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.RouteMatcher;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link ResponseFunction} that is able to transform an error sequence into a custom response.
 */
public class ResumingTransformer implements ResponseFunction {

    private final RouteMatcher routeMatcher;
    private final Predicate<Throwable> predicate;
    private Function<Throwable, Mono<?>> fallback;

    public ResumingTransformer(RouteMatcher routeMatcher, Predicate<Throwable> predicate,
                               Function<Throwable, Mono<?>> fallback) {
        this.routeMatcher = routeMatcher;
        this.predicate = predicate;
        this.fallback = fallback;
    }

    @Override
    public <T> Function<Mono<T>, Mono<T>> transform(DiscordRequest<T> request) {
        if (routeMatcher.matches(request)) {
            return mono -> mono.onErrorResume(predicate, adaptFallback(fallback));
        }
        return mono -> mono;
    }

    @SuppressWarnings("unchecked")
    private <T> Function<? super Throwable, ? extends Mono<? extends T>> adaptFallback(
            Function<? super Throwable, ? extends Mono<?>> fallback) {
        return (Function<? super Throwable, ? extends Mono<? extends T>>) fallback;
    }
}
