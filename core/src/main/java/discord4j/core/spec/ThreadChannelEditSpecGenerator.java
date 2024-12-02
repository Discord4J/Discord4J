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
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ThreadModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface ThreadChannelEditSpecGenerator extends AuditSpec<ThreadModifyRequest> {

    Possible<String> name();

    Possible<Integer> rateLimitPerUser();

    Possible<Boolean> archived();

    Possible<ThreadChannel.AutoArchiveDuration> autoArchiveDuration();

    Possible<Boolean> locked();

    Possible<Boolean> invitable();

    Possible<EnumSet<Channel.Flag>> flags();

    Possible<List<Snowflake>> appliedTags();

    @Override
    default ThreadModifyRequest asRequest() {
        return ThreadModifyRequest.builder()
            .name(name())
            .rateLimitPerUser(rateLimitPerUser())
            .archived(archived())
            .autoArchiveDuration(mapPossible(autoArchiveDuration(), ThreadChannel.AutoArchiveDuration::getValue))
            .locked(locked())
            .invitable(invitable())
            .appliedTags(mapPossible(appliedTags(), tags -> tags.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
            .flags(mapPossible(flags(), Channel.Flag::toBitfield))
            .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class ThreadChannelEditMonoGenerator extends Mono<ThreadChannel> implements ThreadChannelEditSpecGenerator {

    abstract ThreadChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super ThreadChannel> actual) {
        channel().edit(ThreadChannelEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
