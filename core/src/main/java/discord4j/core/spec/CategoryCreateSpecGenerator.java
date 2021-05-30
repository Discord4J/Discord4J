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

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
interface CategoryCreateSpecGenerator extends AuditSpec<ChannelCreateRequest> {

    String name();

    Possible<Integer> position();

    @Value.Default
    default Set<PermissionOverwrite> permissionOverwrites() {
        return Collections.emptySet();
    }

    @Override
    default ChannelCreateRequest asRequest() {
        return ChannelCreateRequest.builder()
                .name(name())
                .position(position())
                .permissionOverwrites(permissionOverwrites().stream()
                        .map(o -> OverwriteData.builder()
                                .id(o.getTargetId().asString())
                                .type(o.getType().getValue())
                                .allow(o.getAllowed().getRawValue())
                                .deny(o.getDenied().getRawValue())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class CategoryCreateMonoGenerator extends Mono<Category> implements CategoryCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Category> actual) {
        guild().createCategory(CategoryCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
