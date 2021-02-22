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

public class OptionKey<T> {

    /** Number of days after which inactive members were kicked. */
    public static final OptionKey<String> DELETE_MEMBER_DAYS = optionKey("delete_member_days", Function.identity());
    /** Number of members removed by the prune. */
    public static final OptionKey<String> MEMBERS_REMOVED = optionKey("members_removed", Function.identity());
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
