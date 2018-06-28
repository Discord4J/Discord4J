/*
 *  This file is part of Discord4J.
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

package discord4j.store.dsl.jvm;

import discord4j.store.dsl.LogicalStatement;
import discord4j.store.dsl.LogicalStatementFactory;
import discord4j.store.dsl.Property;

public class SimpleLogicalStatementFactory<T> implements LogicalStatementFactory<T> {

    private final Class<T> type;

    public SimpleLogicalStatementFactory(Class<T> type) {
        this.type = type;
    }

    @Override
    public <V> LogicalStatement<T> match(Property<T> property, V value) {
        return new SimpleLogicalStatement<>(type, property, value);
    }

    @Override
    public <V extends Comparable<V>> LogicalStatement<T> within(Property<T> property, V start, V end) {
        return new SimpleLogicalStatement<>(type, property, start, end);
    }
}
