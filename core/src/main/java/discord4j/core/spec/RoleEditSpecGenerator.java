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

import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.RoleModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface RoleEditSpecGenerator extends AuditSpec<RoleModifyRequest> {

    Possible<String> name();

    Possible<PermissionSet> permissions();

    Possible<Color> color();

    Possible<Boolean> hoist();

    Possible<Boolean> mentionable();

    @Override
    default RoleModifyRequest asRequest() {
        return RoleModifyRequest.builder()
                .name(name())
                .permissions(mapPossible(permissions(), PermissionSet::getRawValue))
                .color(mapPossible(color(), Color::getRGB))
                .hoist(hoist())
                .mentionable(mentionable())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class RoleEditMonoGenerator extends Mono<Role> implements RoleEditSpecGenerator {

    abstract Role role();

    @Override
    public void subscribe(CoreSubscriber<? super Role> actual) {
        role().edit(RoleEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}