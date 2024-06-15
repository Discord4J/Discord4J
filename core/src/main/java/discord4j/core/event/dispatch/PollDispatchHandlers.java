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
package discord4j.core.event.dispatch;

import discord4j.core.event.domain.poll.PollVoteAddEvent;
import discord4j.core.event.domain.poll.PollVoteRemoveEvent;
import discord4j.discordjson.json.gateway.PollVoteAdd;
import discord4j.discordjson.json.gateway.PollVoteRemove;
import reactor.core.publisher.Mono;

public class PollDispatchHandlers {

    static Mono<PollVoteAddEvent> pollVoteAddHandler(DispatchContext<PollVoteAdd, Void> context) {
        return Mono.just(new PollVoteAddEvent(context.getGateway(), context.getShardInfo(), context.getDispatch()));
    }

    static Mono<PollVoteRemoveEvent> pollVoteRemoveHandler(DispatchContext<PollVoteRemove, Void> context) {
        return Mono.just(new PollVoteRemoveEvent(context.getGateway(), context.getShardInfo(), context.getDispatch()));
    }

}
