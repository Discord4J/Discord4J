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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.AuditEntryInfoData;
import discord4j.discordjson.json.AuditLogChangeData;
import discord4j.core.object.audit.AuditLogChange;
import discord4j.core.object.audit.OptionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AuditLogUtil {

    public static Collector<AuditLogChangeData, ?, Map<String, AuditLogChange<?>>> changeCollector() {
        return Collectors.toMap(
                AuditLogChangeData::key,
                change -> new AuditLogChange<>(change.oldValue().toOptional().orElse(null), change.newValue().toOptional().orElse(null)));
    }

    public static Map<String, ?> createOptionMap(AuditEntryInfoData options) {
        HashMap<String, Object> map = new HashMap<>();
        if (!options.deleteMemberDays().isAbsent()) {
            map.put(OptionKey.DELETE_MEMBER_DAYS.getField(), OptionKey.DELETE_MEMBER_DAYS.parseValue(options.deleteMemberDays().get()));
        }
        if (!options.membersRemoved().isAbsent()) {
            map.put(OptionKey.MEMBERS_REMOVED.getField(), OptionKey.MEMBERS_REMOVED.parseValue(options.membersRemoved().get()));
        }
        if (!options.channelId().isAbsent()) {
            map.put(OptionKey.CHANNEL_ID.getField(), OptionKey.CHANNEL_ID.parseValue(options.channelId().get()));
        }
        if (!options.messageId().isAbsent()) {
            map.put(OptionKey.MESSAGE_ID.getField(), OptionKey.MESSAGE_ID.parseValue(options.messageId().get()));
        }
        if (!options.count().isAbsent()) {
            map.put(OptionKey.COUNT.getField(), OptionKey.COUNT.parseValue(options.count().get()));
        }
        if (!options.id().isAbsent()) {
            map.put(OptionKey.ID.getField(), OptionKey.ID.parseValue(options.id().get()));
        }
        if (!options.type().isAbsent()) {
            map.put(OptionKey.TYPE.getField(), OptionKey.TYPE.parseValue(options.type().get()));
        }
        if (!options.roleName().isAbsent()) {
            map.put(OptionKey.ROLE_NAME.getField(), OptionKey.ROLE_NAME.parseValue(options.roleName().get()));
        }
        return map;
    }

}
