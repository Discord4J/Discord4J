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
package discord4j.core.util;

import discord4j.common.json.response.AuditLogChangeResponse;
import discord4j.common.json.response.AuditLogEntryOptionsResponse;
import discord4j.core.object.audit.AuditLogChange;
import discord4j.core.object.audit.OptionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AuditLogUtil {

    public static Collector<AuditLogChangeResponse, ?, Map<String, AuditLogChange<?>>> changeCollector() {
        return Collectors.toMap(
                AuditLogChangeResponse::getKey,
                change -> new AuditLogChange<>(change.getOldValue(), change.getNewValue()));
    }

    public static Map<String, ?> createOptionMap(AuditLogEntryOptionsResponse options) {
        HashMap<String, Object> map = new HashMap<>();
        if (options.getDeleteMemberDays() != null) {
            map.put(OptionKey.DELETE_MEMBER_DAYS.getField(), options.getDeleteMemberDays());
        }
        if (options.getMembersRemoved() != null) {
            map.put(OptionKey.MEMBERS_REMOVED.getField(), options.getMembersRemoved());
        }
        if (options.getChannelId() != null) {
            map.put(OptionKey.CHANNEL_ID.getField(), options.getChannelId());
        }
        if (options.getCount() != null) {
            map.put(OptionKey.COUNT.getField(), options.getCount());
        }
        if (options.getId() != null) {
            map.put(OptionKey.ID.getField(), options.getId());
        }
        if (options.getType() != null) {
            map.put(OptionKey.TYPE.getField(), options.getType());
        }
        if (options.getRoleName() != null) {
            map.put(OptionKey.ROLE_NAME.getField(), options.getRoleName());
        }
        return map;
    }

}
