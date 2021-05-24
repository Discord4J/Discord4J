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
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.audit.AuditLogPart;
import discord4j.core.object.entity.Guild;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static discord4j.core.spec.InternalSpecUtils.mapNullable;
import static discord4j.core.spec.InternalSpecUtils.putIfNotNull;

@SpecStyle
@Value.Immutable(singleton = true)
interface AuditLogQuerySpecGenerator extends Spec<Map<String, Object>> {
    
    @Nullable
    Snowflake userId();
    
    @Nullable
    ActionType actionType();
    
    @Nullable
    Snowflake before();
    
    @Nullable
    Integer limit();

    @Override
    default Map<String, Object> asRequest() {
        Map<String, Object> request = new HashMap<>(4);
        putIfNotNull(request, "user_id", mapNullable(userId(), Snowflake::asString));
        putIfNotNull(request, "action_type", mapNullable(actionType(), ActionType::getValue));
        putIfNotNull(request, "before", mapNullable(before(), Snowflake::asString));
        putIfNotNull(request, "limit", limit());
        return request;
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class AuditLogQueryFluxGenerator extends Flux<AuditLogPart> implements AuditLogQuerySpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super AuditLogPart> actual) {
        guild().getAuditLog(AuditLogQuerySpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
