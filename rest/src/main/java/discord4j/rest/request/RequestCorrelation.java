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

package discord4j.rest.request;

import reactor.core.publisher.Sinks;
import reactor.util.context.ContextView;

class RequestCorrelation<T> {

    private final DiscordWebRequest request;
    private final Sinks.One<T> response;
    private final ContextView context;

    RequestCorrelation(DiscordWebRequest request, Sinks.One<T> response, ContextView context) {
        this.request = request;
        this.response = response;
        this.context = context;
    }

    public DiscordWebRequest getRequest() {
        return request;
    }

    public Sinks.One<T> getResponse() {
        return response;
    }

    public ContextView getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "RequestCorrelation{" +
                "request=" + request.getDescription() +
                ", response=" + response +
                ", context=" + context +
                '}';
    }
}
