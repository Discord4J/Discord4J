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
package discord4j.core.object.audit;

import discord4j.common.util.Snowflake;

import java.util.function.Function;

/**
 * A key to be used in {@link AuditLogEntry#getOption(OptionKey)}.
 *
 * @param <T> The type of the optional data.
 *
 * @see <a href="https://discord.com/developers/docs/resources/audit-log#audit-log-entry-object-optional-audit-entry-info">Optional Audit Entry Info</a>
 */
public class OptionKey<T> {

    /** Number of days after which inactive members were kicked. */
    public static final OptionKey<Integer> DELETE_MEMBER_DAYS = optionKey("delete_member_days", Integer::parseInt);
    /** Number of members removed by the prune. */
    public static final OptionKey<Integer> MEMBERS_REMOVED = optionKey("members_removed", Integer::parseInt);
    /** Channel in which the entities were targeted. */
    public static final OptionKey<Snowflake> CHANNEL_ID = optionKey("channel_id", Snowflake::of);
    /** Id of the message that was targeted. */
    public static final OptionKey<Snowflake> MESSAGE_ID = optionKey("message_id", Snowflake::of);
    /** Number of entities that were targeted. */
    public static final OptionKey<Integer> COUNT = optionKey("count", Integer::parseInt);
    /** Id of the overwritten entity. */
    public static final OptionKey<Snowflake> ID = optionKey("id", Snowflake::of);
    /** Type of overwritten entity ("member" or "role"). */
    public static final OptionKey<String> TYPE = optionKey("type", Function.identity());
    /** Name of the role if type is "role". */
    public static final OptionKey<String> ROLE_NAME = optionKey("role_name", Function.identity());
    /** The type of integration which performed the action. */
    public static final OptionKey<String> INTEGRATION_TYPE = optionKey("integration_type", Function.identity());
    /** Name of the Auto Moderation rule that was triggered **/
    public static final OptionKey<String> AUTO_MODERATION_RULE_NAME = optionKey("auto_moderation_rule_name", Function.identity());
    /** Trigger type of the Auto Moderation rule that was triggered **/
    public static final OptionKey<String> AUTO_MODERATION_RULE_TRIGGER_TYPE = optionKey("auto_moderation_rule_trigger_type", Function.identity());
    /** ID of the app whose permissions were targeted. */
    public static final OptionKey<Snowflake> APPLICATION_ID = optionKey("application_id", Snowflake::of);

    private static <T> OptionKey<T> optionKey(String field, Function<String, T> parser) {
        return new OptionKey<>(field, parser);
    }

    private final String field;
    private final Function<String, T> parser;

    private OptionKey(String field, Function<String, T> parser) {
        this.field = field;
        this.parser = parser;
    }

    public String getField() {
        return field;
    }

    public T parseValue(String value) {
        return parser.apply(value);
    }

    @Override
    public String toString() {
        return "OptionKey{" +
                "field='" + field + '\'' +
                '}';
    }
}
