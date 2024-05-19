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
import discord4j.core.object.BulkBan;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.BulkBanRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
interface BulkBanRequestSpecGenerator extends AuditSpec<BulkBanRequest> {

    List<Snowflake> userIds();

    Possible<Integer> deleteMessageSeconds();

    @Override
    default BulkBanRequest asRequest() {
        return BulkBanRequest.builder()
            .userIds(userIds().stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList()))
            .deleteMessageSeconds(deleteMessageSeconds())
            .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class BulkBanRequestMonoGenerator extends Mono<BulkBan> implements BulkBanRequestSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super BulkBan> actual) {
        guild().bulkBan(BulkBanRequestSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
