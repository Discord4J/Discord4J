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

public class OptionKey<T> {

    /** Number of days after which inactive members were kicked. */
    public static final OptionKey<String> DELETE_MEMBER_DAYS = optionKey("delete_member_days");
    /** Number of members removed by the prune. */
    public static final OptionKey<String> MEMBERS_REMOVED = optionKey("members_removed");
    /** Channel in which the entities were targeted. */
    public static final OptionKey<Snowflake> CHANNEL_ID = optionKey("channel_id");
    /** Id of the message that was targeted. */
    public static final OptionKey<Snowflake> MESSAGE_ID = optionKey("message_id");
    /** Number of entities that were targeted. */
    public static final OptionKey<Integer> COUNT = optionKey("count");
    /** Id of the overwritten entity. */
    public static final OptionKey<Snowflake> ID = optionKey("id");
    /** Type of overwritten entity ("member" or "role"). */
    public static final OptionKey<String> TYPE = optionKey("type");
    /** Name of the role if type is "role". */
    public static final OptionKey<String> ROLE_NAME = optionKey("role_name");

    private static <T> OptionKey<T> optionKey(String field) {
        return new OptionKey<>(field);
    }

    private final String field;

    private OptionKey(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return "OptionKey{" +
                "field='" + field + '\'' +
                '}';
    }
}
