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
import discord4j.discordjson.Id;
import discord4j.discordjson.json.InviteCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface InviteCreateSpecGenerator extends AuditSpec<MultipartRequest<InviteCreateRequest>> {

    Possible<Integer> maxAge();

    Possible<Integer> maxUses();

    Possible<Boolean> temporary();

    Possible<Boolean> unique();

    Possible<Invite.Type> targetType();

    Possible<Snowflake> targetUserId();

    Possible<Snowflake> targetApplicationId();

    Possible<InviteCreateFields.File> targetUsersFile();

    Possible<List<Snowflake>> roleIds();

    @Override
    default MultipartRequest<InviteCreateRequest> asRequest() {
        InviteCreateRequest jsonRequest = InviteCreateRequest.builder()
            .maxAge(maxAge())
            .maxUses(maxUses())
            .temporary(temporary())
            .unique(unique())
            .targetType(mapPossible(targetType(), Invite.Type::getValue))
            .targetUserId(mapPossible(targetUserId(), Snowflake::asString))
            .targetApplicationId(mapPossible(targetApplicationId(), Snowflake::asString))
            .roleIds(mapPossible(roleIds(), r -> r.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
            .build();

        MultipartRequest<InviteCreateRequest> inviteCreateRequestMultipartRequest = MultipartRequest.ofRequest(jsonRequest, "target_users_file");
        if (this.targetUsersFile().isPresent()) {
            inviteCreateRequestMultipartRequest = inviteCreateRequestMultipartRequest.addFile(this.targetUsersFile().get().name(), this.targetUsersFile().get().inputStream());
        }

        return inviteCreateRequestMultipartRequest;
    }
}

@SuppressWarnings("immutables:subtype")
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
