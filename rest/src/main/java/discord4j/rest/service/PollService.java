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
package discord4j.rest.service;

import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.gateway.PollVoters;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.Multimap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PollService extends RestService {

    public PollService(Router router) {
        super(router);
    }

    public Mono<MessageData> endPoll(long channelId, long messageId) {
        return Routes.END_POLL.newRequest(channelId, messageId)
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Flux<UserData> getPollVoters(long channelId, long messageId, int answerId, Multimap<String, Object> queryParams) {
        return Routes.POLL_ANSWER_VOTERS_GET.newRequest(channelId, messageId, answerId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(PollVoters.class)
            .map(PollVoters::users)
            .flatMapMany(Flux::fromIterable);
    }

}
