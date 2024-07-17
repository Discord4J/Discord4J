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
package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Multimap;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import static discord4j.core.spec.InternalSpecUtils.setIfPresent;

@Value.Immutable
public interface PollVotersRequestSpecGenerator extends Spec<Multimap<String, Object>> {

    Possible<Snowflake> after();

    Possible<Integer> limit();

    @Override
    default Multimap<String, Object> asRequest() {
        Multimap<String, Object> map = new Multimap<>();

        setIfPresent(map, "after", after());
        setIfPresent(map, "limit", limit());

        return map;
    }

}

@Value.Immutable(builder = false)
abstract class PollVotersRequestFluxGenerator extends Flux<User> implements PollVotersRequestSpecGenerator {

    abstract GatewayDiscordClient client();

    abstract DiscordClient restClient();

    abstract Snowflake channelId();

    abstract Snowflake messageId();

    abstract int answerId();

    @Override
    public void subscribe(CoreSubscriber<? super User> coreSubscriber) {
        restClient().getPollService().getPollVoters(
                channelId().asLong(),
                messageId().asLong(),
                answerId(),
                this.asRequest()
            ).map(userData -> new User(client(), userData))
            .subscribe(coreSubscriber);
    }

    @Override
    public abstract String toString();
}
