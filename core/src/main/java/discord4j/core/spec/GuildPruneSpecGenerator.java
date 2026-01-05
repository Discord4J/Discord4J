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
import discord4j.rest.util.Multimap;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.*;

@Value.Immutable
interface GuildPruneSpecGenerator extends AuditSpec<Multimap<String, Object>> {

    Integer days();

    @Nullable
    Set<Snowflake> roles();

    @Nullable
    Boolean computePruneCount();

    @Override
    default Multimap<String, Object> asRequest() {
        Multimap<String, Object> map = new Multimap<>();
        setIfNotNull(map, "days", days());
        addAllIfNotNull(map, "include_roles", mapNullable(roles(), r -> r.stream()
                .map(Snowflake::asString)
                .collect(Collectors.toList())));
        setIfNotNull(map, "compute_prune_count", computePruneCount());
        return map;
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildPruneMonoGenerator extends Mono<Integer> implements GuildPruneSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Integer> actual) {
        guild().prune(GuildPruneSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
