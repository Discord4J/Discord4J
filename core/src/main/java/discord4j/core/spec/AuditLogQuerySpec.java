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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.spec;

import discord4j.core.object.audit.ActionType;
import discord4j.core.object.util.Snowflake;

import java.util.HashMap;
import java.util.Map;

public final class AuditLogQuerySpec implements Spec<Map<String, Object>> {

    private final Map<String, Object> request = new HashMap<>(2);

    public AuditLogQuerySpec setResponsibleUser(final Snowflake userId) {
        request.put("user_id", userId.asLong());
        return this;
    }

    public AuditLogQuerySpec setActionType(final ActionType actionType) {
        request.put("action_type", actionType.getValue());
        return this;
    }

    @Override
    public Map<String, Object> asRequest() {
        return request;
    }
}
