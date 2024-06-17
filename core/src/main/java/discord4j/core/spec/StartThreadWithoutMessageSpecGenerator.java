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

import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageWithThreadsChannel;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

/**
 * A spec to create threads that are not connected to a message.
 *
 * <ul>
 *     <li><strong>name</strong>: 1-100 character channel name</li>
 *     <li><strong>autoArchiveDuration</strong>: duration in minutes to automatically archive the thread after recent
 *     activity, can be set to: 60, 1440, 4320, 10080</li>
 *     <li><strong>type</strong>: the type of thread to create</li>
 *     <li><strong>invitable</strong>: whether non-moderators can add other non-moderators to a thread; only
 *     available when creating a private thread</li>
 *     <li><strong>rateLimitPerUser</strong>: amount of seconds a user has to wait before sending another message
 *     (0-21600)</li>
 * </ul>
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#start-thread-without-message">Documentation</a>
 */
@Value.Immutable
public interface StartThreadWithoutMessageSpecGenerator extends AuditSpec<StartThreadWithoutMessageRequest> {

    String name();

    Possible<ThreadChannel.AutoArchiveDuration> autoArchiveDuration();

    ThreadChannel.Type type();

    Possible<Boolean> invitable();

    Possible<Integer> rateLimitPerUser();

    @Override
    default StartThreadWithoutMessageRequest asRequest() {
        return StartThreadWithoutMessageRequest.builder()
                .name(name())
                .autoArchiveDuration(mapPossible(autoArchiveDuration(), ThreadChannel.AutoArchiveDuration::getValue))
                .type(type().getValue())
                .invitable(invitable())
                .rateLimitPerUser(rateLimitPerUser())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class StartThreadWithoutMessageMonoGenerator extends Mono<ThreadChannel> implements StartThreadWithoutMessageSpecGenerator {

    abstract TopLevelGuildMessageWithThreadsChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super ThreadChannel> actual) {
        TopLevelGuildMessageWithThreadsChannel channel = channel();

        if (this.type().isPublic()) {
            channel.startPublicThreadWithoutMessage(StartThreadWithoutMessageSpec.copyOf(this)).subscribe(actual);
        } else if (channel instanceof TextChannel) {
            ((TextChannel) channel).startPrivateThread(StartThreadWithoutMessageSpec.copyOf(this)).subscribe(actual);
        } else {
            Mono.<ThreadChannel>error(new IllegalArgumentException(this.type().name() + " type cannot be used to start a thread without a message in a " + channel.getType().name() + " channel!"))
                .subscribe(actual);
        }
    }

    @Override
    public abstract String toString();

}
