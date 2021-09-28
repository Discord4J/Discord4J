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
import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOverwrites;

@Value.Immutable(singleton = true)
interface AudioChannelEditSpecGenerator extends AuditSpec<ChannelModifyRequest> {

    Possible<String> name();

    Possible<Integer> bitrate();

    Possible<Integer> position();

    Possible<List<PermissionOverwrite>> permissionOverwrites();

    Possible<Optional<Snowflake>> parentId();

    Possible<Optional<String>> rtcRegion();

    @Override
    default ChannelModifyRequest asRequest() {
        return ChannelModifyRequest.builder()
                .name(name())
                .bitrate(bitrate())
                .position(position())
                .permissionOverwrites(mapPossibleOverwrites(permissionOverwrites()))
                .parentId(mapPossibleOptional(parentId(), Snowflake::asString))
                .rtcRegion(rtcRegion())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AudioChannelEditMonoGenerator extends Mono<AudioChannel> implements AudioChannelEditSpecGenerator {

    abstract AudioChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super AudioChannel> actual) {
        channel().edit(AudioChannelEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
