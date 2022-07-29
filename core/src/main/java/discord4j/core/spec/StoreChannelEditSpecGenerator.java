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
import discord4j.core.object.entity.channel.StoreChannel;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface StoreChannelEditSpecGenerator extends AuditSpec<ChannelModifyRequest> {

    Possible<String> name();

    Possible<Integer> position();

    Possible</*~~>*/List<PermissionOverwrite>> permissionOverwrites();

    Possible<Optional<Snowflake>> parentId();

    @Override
    default ChannelModifyRequest asRequest() {
        return ChannelModifyRequest.builder()
                .name(name())
                .position(position())
                .permissionOverwrites(InternalSpecUtils.mapPossible(permissionOverwrites(), po -> po.stream()
                        .map(PermissionOverwrite::getData)
                        .collect(Collectors.toList())))
                .parentId(mapPossibleOptional(parentId(), Snowflake::asString))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class StoreChannelEditMonoGenerator extends Mono<StoreChannel> implements StoreChannelEditSpecGenerator {

    abstract StoreChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super StoreChannel> actual) {
        channel().edit(StoreChannelEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
