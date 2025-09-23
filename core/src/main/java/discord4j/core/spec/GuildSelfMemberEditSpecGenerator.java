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

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.CurrentMemberModifyData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface GuildSelfMemberEditSpecGenerator extends AuditSpec<CurrentMemberModifyData> {

    Possible<Optional<String>> nick();

    Possible<Optional<Image>> banner();

    Possible<Optional<Image>> avatar();

    Possible<Optional<String>> bio();

    @Override
    default CurrentMemberModifyData asRequest() {
        return CurrentMemberModifyData.builder()
            .nick(nick())
            .banner(mapPossibleOptional(banner(), Image::getDataUri))
            .avatar(mapPossibleOptional(avatar(), Image::getDataUri))
            .bio(bio())
            .build();
    }

}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildSelfMemberEditMonoGenerator extends Mono<Member> implements GuildSelfMemberEditSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Member> actual) {
        guild().editSelfMember(GuildSelfMemberEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
