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

import discord4j.discordjson.json.AuditEntryInfoData;
import discord4j.discordjson.json.AuditLogChangeData;
import discord4j.core.object.audit.OptionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AuditLogUtil {

    public static Collector<AuditLogChangeData, ?, Map<String, AuditLogChangeData>> changeCollector() {
        return Collectors.toMap(AuditLogChangeData::key, Function.identity());
    }

    public static Map<String, String> createOptionMap(AuditEntryInfoData options) {
        HashMap<String, String> map = new HashMap<>();
        options.deleteMemberDays().toOptional().ifPresent(it -> map.put(OptionKey.DELETE_MEMBER_DAYS.getField(), it));
        options.membersRemoved().toOptional().ifPresent(it -> map.put(OptionKey.MEMBERS_REMOVED.getField(), it));
        options.channelId().toOptional().ifPresent(it -> map.put(OptionKey.CHANNEL_ID.getField(), it.asString()));
        options.messageId().toOptional().ifPresent(it -> map.put(OptionKey.MESSAGE_ID.getField(), it.asString()));
        options.count().toOptional().ifPresent(it -> map.put(OptionKey.COUNT.getField(), it));
        options.id().toOptional().ifPresent(it -> map.put(OptionKey.ID.getField(), it.asString()));
        options.type().toOptional().ifPresent(it -> map.put(OptionKey.TYPE.getField(), it));
        options.roleName().toOptional().ifPresent(it -> map.put(OptionKey.ROLE_NAME.getField(), it));
        options.integrationType().toOptional().ifPresent(it -> map.put(OptionKey.INTEGRATION_TYPE.getField(), it));
        options.autoModerationRuleName().toOptional().ifPresent(it -> map.put(OptionKey.AUTO_MODERATION_RULE_NAME.getField(), it));
        options.autoModerationRuleTriggerType().toOptional().ifPresent(it -> map.put(OptionKey.AUTO_MODERATION_RULE_TRIGGER_TYPE.getField(), it));
        options.applicationId().toOptional().ifPresent(it -> map.put(OptionKey.APPLICATION_ID.getField(), it.asString()));
        return map;
    }

}
