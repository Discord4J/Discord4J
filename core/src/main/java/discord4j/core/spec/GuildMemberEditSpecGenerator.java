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
import discord4j.discordjson.json.GuildMemberModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Set;

import static discord4j.core.spec.InternalSpecUtils.toPossible;
import static discord4j.core.spec.InternalSpecUtils.toPossibleOptional;

@SpecStyle
@Value.Immutable(singleton = true)
interface GuildMemberEditSpecGenerator extends AuditSpec<GuildMemberModifyRequest> {

    @Value.Default
    @Nullable
    default Possible<Snowflake> newVoiceChannel() {
        return Possible.absent();
    }
    
    @Nullable
    Boolean mute();
    
    @Nullable
    Boolean deafen();

    @Value.Default
    @Nullable
    default Possible<String> nickname() {
        return Possible.absent();
    }
    
    @Nullable
    Set<Snowflake> roles();

    @Override
    default GuildMemberModifyRequest asRequest() {
        return GuildMemberModifyRequest.builder()
                .channelId(toPossibleOptional(newVoiceChannel(), Snowflake::asString))
                .mute(toPossible(mute()))
                .deaf(toPossible(deafen()))
                .nick(toPossibleOptional(nickname()))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class GuildMemberEditMonoGenerator extends Mono<Member> implements GuildMemberEditSpecGenerator {

    abstract Member member();

    @Override
    public void subscribe(CoreSubscriber<? super Member> actual) {
        member().edit(GuildMemberEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}