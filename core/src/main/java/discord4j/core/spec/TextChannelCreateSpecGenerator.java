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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
interface TextChannelCreateSpecGenerator extends AuditSpec<ChannelCreateRequest> {

    String name();

    Possible<String> topic();

    Possible<Integer> rateLimitPerUser();

    Possible<Integer> position();

    Possible</*~~>*/List<PermissionOverwrite>> permissionOverwrites();

    Possible<Snowflake> parentId();

    Possible<Boolean> nsfw();

    @Override
    default ChannelCreateRequest asRequest() {
        return ChannelCreateRequest.builder()
                .type(Channel.Type.GUILD_TEXT.getValue())
                .name(name())
                .topic(topic())
                .rateLimitPerUser(rateLimitPerUser())
                .position(position())
                .permissionOverwrites(mapPossible(permissionOverwrites(), po -> po.stream()
                        .map(PermissionOverwrite::getData)
                        .collect(Collectors.toList())))
                .parentId(mapPossible(parentId(), Snowflake::asString))
                .nsfw(nsfw())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class TextChannelCreateMonoGenerator extends Mono<TextChannel> implements TextChannelCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super TextChannel> actual) {
        guild().createTextChannel(TextChannelCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
