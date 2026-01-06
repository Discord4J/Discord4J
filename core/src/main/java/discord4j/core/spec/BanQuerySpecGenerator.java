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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.PartialMember;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static discord4j.core.spec.InternalSpecUtils.putIfNotNull;

@Value.Immutable(singleton = true)
interface BanQuerySpecGenerator extends AuditSpec<Map<String, Object>> {

    @Nullable
    Integer deleteMessageSeconds();

    @Deprecated
    @Nullable Integer deleteMessageDays();

    @Override
    default Map<String, Object> asRequest() {
        Map<String, Object> request = new HashMap<>(2);
        putIfNotNull(request, "delete_message_seconds", deleteMessageSeconds());
        putIfNotNull(request, "delete_message_days", deleteMessageDays());
        putIfNotNull(request, "reason", reason());
        return request;
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildBanQueryMonoGenerator extends Mono<Void> implements BanQuerySpecGenerator {

    abstract Snowflake userId();

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        guild().ban(userId(), BanQuerySpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class MemberBanQueryMonoGenerator extends Mono<Void> implements BanQuerySpecGenerator {

    abstract PartialMember member();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        member().ban(BanQuerySpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
