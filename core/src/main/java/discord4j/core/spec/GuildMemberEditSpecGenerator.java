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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.PartialMember;
import discord4j.discordjson.json.GuildMemberModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface GuildMemberEditSpecGenerator extends AuditSpec<GuildMemberModifyRequest> {

    Possible<Optional<Snowflake>> newVoiceChannel();

    Possible<Boolean> mute();

    Possible<Boolean> deafen();

    Possible<Optional<String>> nickname();

    Possible<List<Snowflake>> roles();

    Possible<Optional<Instant>> communicationDisabledUntil();

    @Override
    default GuildMemberModifyRequest asRequest() {
        return GuildMemberModifyRequest.builder()
                .channelId(mapPossibleOptional(newVoiceChannel(), Snowflake::asString))
                .mute(mute())
                .deaf(deafen())
                .nick(nickname())
                .roles(mapPossible(roles(), r -> r.stream().map(Snowflake::asString).collect(Collectors.toList())))
                .communicationDisabledUntil(mapPossibleOptional(communicationDisabledUntil(), Instant::toString))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildMemberEditMonoGenerator extends Mono<Member> implements GuildMemberEditSpecGenerator {

    abstract PartialMember member();

    @Override
    public void subscribe(CoreSubscriber<? super Member> actual) {
        member().edit(GuildMemberEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
