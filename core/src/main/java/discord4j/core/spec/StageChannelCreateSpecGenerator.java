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
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOverwrites;

@Value.Immutable
interface StageChannelCreateSpecGenerator extends AuditSpec<ChannelCreateRequest> {

    String name();

    Possible<Integer> bitrate();

    Possible<Integer> position();

    Possible<List<PermissionOverwrite>> permissionOverwrites();

    Possible<Snowflake> parentId();

    @Override
    default ChannelCreateRequest asRequest() {
        return ChannelCreateRequest.builder()
                .type(Channel.Type.GUILD_STAGE_VOICE.getValue())
                .name(name())
                .bitrate(bitrate())
                .position(position())
                .permissionOverwrites(mapPossibleOverwrites(permissionOverwrites()))
                .parentId(mapPossible(parentId(), Snowflake::asString))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class StageChannelCreateMonoGenerator extends Mono<VoiceChannel> implements StageChannelCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super VoiceChannel> actual) {
        guild().createStageChannel(StageChannelCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}