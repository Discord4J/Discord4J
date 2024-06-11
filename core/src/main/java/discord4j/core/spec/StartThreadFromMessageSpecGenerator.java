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

package discord4j.core.spec;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.json.StartThreadFromMessageRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

/**
 * A spec to create threads from a message.
 *
 * <ul>
 *     <li><strong>name</strong>: 1-100 character channel name</li>
 *     <li><strong>autoArchiveDuration</strong>: duration in minutes to automatically archive the thread after recent
 *     activity, can be set to: 60, 1440, 4320, 10080</li>
 *     <li><strong>rateLimitPerUser</strong>: amount of seconds a user has to wait before sending another message
 *     (0-21600)</li>
 * </ul>
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#start-thread-from-message">Documentation</a>
 */
@Value.Immutable
public interface StartThreadFromMessageSpecGenerator extends AuditSpec<StartThreadFromMessageRequest> {

    String name();

    Possible<ThreadChannel.AutoArchiveDuration> autoArchiveDuration();

    Possible<Integer> rateLimitPerUser();

    @Override
    default StartThreadFromMessageRequest asRequest() {
        return StartThreadFromMessageRequest.builder()
                .name(name())
                .autoArchiveDuration(mapPossible(autoArchiveDuration(), ThreadChannel.AutoArchiveDuration::getValue))
                .rateLimitPerUser(rateLimitPerUser())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class StartThreadFromMessageMonoGenerator extends Mono<ThreadChannel> implements StartThreadFromMessageSpecGenerator {

    abstract Message message();

    @Override
    public void subscribe(CoreSubscriber<? super ThreadChannel> actual) {
        message().startThread(StartThreadFromMessageSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();

}
