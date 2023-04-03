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

import discord4j.common.util.Snowflake;
import discord4j.core.object.Region;
import discord4j.core.object.entity.channel.ForumChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.StartThreadInForumChannelRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

/**
 * A spec to create threads in a forum channel
 *
 * <ul>
 *     <li><strong>name</strong>: 1-100 character channel name</li>
 *     <li><strong>autoArchiveDuration</strong>: duration in minutes to automatically archive the thread after recent
 *     activity, can be set to: 60, 1440, 4320, 10080</li>
 *     <li><strong>rateLimitPerUser</strong>: amount of seconds a user has to wait before sending another message
 *     (0-21600)</li>
 *     <li><strong>message</strong>: a forum thread message object to start the thread with</li>
 *     <li><strong>applied_tags</strong>: array of tag snowflakes to apply on this thread</li>
 * </ul>
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#start-thread-in-forum-channel">Documentation</a>
 */
@Value.Immutable
public interface StartThreadInForumChannelSpecGenerator extends AuditSpec<StartThreadInForumChannelRequest> {

    String name();

    ForumThreadMessageCreateSpec message();

    Possible<ThreadChannel.AutoArchiveDuration> autoArchiveDuration();

    Possible<Optional<Integer>> rateLimitPerUser();

    Possible<List<Snowflake>> appliedTags();

    @Override
    default StartThreadInForumChannelRequest asRequest() {
        return StartThreadInForumChannelRequest.builder()
            .name(name())
            .message(message().asRequest())
            .autoArchiveDuration(mapPossible(autoArchiveDuration(), ThreadChannel.AutoArchiveDuration::getValue))
            .rateLimitPerUser(rateLimitPerUser())
            .appliedTags(mapPossible(appliedTags(), list -> list.stream().map(snowflake -> Id.of(snowflake.asLong())).collect(Collectors.toList())))
            .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class StartThreadInForumChannelMonoGenerator extends Mono<ThreadChannel> implements StartThreadInForumChannelSpecGenerator {

    abstract ForumChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super ThreadChannel> actual) {
        channel().startThread(StartThreadInForumChannelSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();

}
