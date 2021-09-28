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
package discord4j.core.spec.legacy;

import discord4j.core.object.audit.ActionType;
import discord4j.common.util.Snowflake;

import java.util.HashMap;
import java.util.Map;

/** A spec used to query audit log entries that match the specified filter. */
public final class LegacyAuditLogQuerySpec implements LegacySpec<Map<String, Object>> {

    private final Map<String, Object> request = new HashMap<>(4);

    /**
     * Sets the query to only return entries where the user specified is responsible for the action.
     *
     * @param userId The {@code Snowflake} of the responsible user to filter by.
     * @return This spec.
     */
    public LegacyAuditLogQuerySpec setResponsibleUser(final Snowflake userId) {
        request.put("user_id", userId.asLong());
        return this;
    }

    /**
     * Sets the query to return entries where the action type is the same as the one provided.
     *
     * @param actionType The {@code ActionType} of the audit log entries to filter by.
     * @return This spec.
     */
    public LegacyAuditLogQuerySpec setActionType(final ActionType actionType) {
        request.put("action_type", actionType.getValue());
        return this;
    }

    /**
     * Sets the query to return entries before a certain entry id.
     *
     * @param beforeId The {@code Snowflake} of the audit log id to filter by before of.
     * @return This spec.
     */
    public LegacyAuditLogQuerySpec setBefore(final Snowflake beforeId) {
        request.put("before", beforeId.asString());
        return this;
    }

    /**
     * Sets the query to return a max of entries.
     *
     * @param limit The limit of the audit log entries to filter by.
     * @return This spec.
     */
    public LegacyAuditLogQuerySpec setLimit(final int limit) {
        request.put("limit", limit);
        return this;
    }

    @Override
    public Map<String, Object> asRequest() {
        return request;
    }
}
