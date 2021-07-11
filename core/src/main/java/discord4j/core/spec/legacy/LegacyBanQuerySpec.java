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

import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/** A spec used to configure a user ban. */
public final class LegacyBanQuerySpec implements LegacyAuditSpec<Map<String, Object>> {

    private final Map<String, Object> request = new HashMap<>(2);

    /**
     * Sets the number of days to delete messages for (0-7).
     *
     * @param days A number from 0 to 7 indicating how many days of messages should be deleted when a user is banned.
     * @return This spec.
     */
    public LegacyBanQuerySpec setDeleteMessageDays(final int days) {
        request.put("delete_message_days", days);
        return this;
    }

    @Override
    public LegacyBanQuerySpec setReason(@Nullable final String reason) {
        request.put("reason", reason);
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return (String) request.get("reason");
    }

    @Override
    public Map<String, Object> asRequest() {
        return request;
    }
}
