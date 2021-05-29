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
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.Invite;
import discord4j.core.object.entity.channel.CategorizableChannel;
import discord4j.discordjson.json.InviteCreateRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@SpecStyle
@Value.Immutable(singleton = true)
interface InviteCreateSpecGenerator extends AuditSpec<InviteCreateRequest> {

    Possible<Integer> maxAge();

    Possible<Integer> maxUses();

    Possible<Boolean> temporary();

    Possible<Boolean> unique();

    Possible<Invite.Type> targetType();

    Possible<Snowflake> targetUserId();

    Possible<Snowflake> targetApplicationId();

    @Override
    default InviteCreateRequest asRequest() {
        return InviteCreateRequest.builder()
                .maxAge(maxAge())
                .maxUses(maxUses())
                .temporary(temporary())
                .unique(unique())
                .targetType(mapPossible(targetType(), Invite.Type::getValue))
                .targetUserId(mapPossible(targetUserId(), Snowflake::asString))
                .targetApplicationId(mapPossible(targetApplicationId(), Snowflake::asString))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class InviteCreateMonoGenerator extends Mono<ExtendedInvite> implements InviteCreateSpecGenerator {

    abstract CategorizableChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super ExtendedInvite> actual) {
        channel().createInvite(InviteCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
