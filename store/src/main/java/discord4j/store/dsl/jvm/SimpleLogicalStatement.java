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
import discord4j.store.dsl.Property;
import discord4j.store.util.WithinRangePredicate;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

public class SimpleLogicalStatement<T, V> implements LogicalStatement<T> {

    @Nullable
    private final MethodHandle handle;
    private final Predicate<T> tester;

    public SimpleLogicalStatement(Class<T> holder, Property<T> property, @Nullable V value) {
        String getterName = "get" + Character.toTitleCase(property.getName().charAt(0)) + property.getName().substring(1);
        try {
            Method method = holder.getMethod(getterName);
            method.setAccessible(true);
            this.handle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.tester = (t) -> {
            try {
                return t == null ? value == null : Objects.equals(handle.bindTo(t).invoke(), value);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    public SimpleLogicalStatement(Class<T> holder, Property<T> property, V start, V end) {
        String getterName = "get" + Character.toTitleCase(property.getName().charAt(0)) + property.getName().substring(1);
        try {
            Method method = holder.getMethod(getterName);
            method.setAccessible(true);
            this.handle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if (!(start instanceof Comparable || end instanceof Comparable))
            throw new RuntimeException("This constructor overload requires a comparable type!");

        Predicate<V> predicate = new WithinRangePredicate((Comparable<V>) start, (Comparable<V>) end);
        this.tester = (t) -> {
            try {
                return t == null ? start == null || end == null : predicate.test((V) handle.bindTo(t).invoke());
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    private SimpleLogicalStatement(Predicate<T> override) {
        this.tester = override;
        this.handle = null;
    }

    @Override
    public LogicalStatement<T> not() {
        return new SimpleLogicalStatement<T, V>(t -> !this.test(t));
    }

    @Override
    public LogicalStatement<T> and(LogicalStatement<T>... others) {
        return new SimpleLogicalStatement<T, V>(t -> {
            if (!this.test(t))
                return false;
            for (LogicalStatement<T> stmt : others) {
                if (!(stmt instanceof SimpleLogicalStatement))
                    throw new RuntimeException("Incompatible statements used!");
                if (!((SimpleLogicalStatement<T, V>) stmt).test(t))
                    return false;
            }
            return true;
        });
    }

    @Override
    public LogicalStatement<T> or(LogicalStatement<T>... others) {
        return new SimpleLogicalStatement<T, V>(t -> {
            if (this.test(t))
                return true;
            for (LogicalStatement<T> stmt : others) {
                if (!(stmt instanceof SimpleLogicalStatement))
                    throw new RuntimeException("Incompatible statements used!");
                if (((SimpleLogicalStatement<T, V>) stmt).test(t))
                    return true;
            }
            return false;
        });
    }

    @Override
    public LogicalStatement<T> xor(LogicalStatement<T>... others) {
        return new SimpleLogicalStatement<T, V>(t -> {
            boolean currValue = this.test(t);
            for (LogicalStatement<T> stmt : others) {
                if (!(stmt instanceof SimpleLogicalStatement))
                    throw new RuntimeException("Incompatible statements used!");
                if (((SimpleLogicalStatement<T, V>) stmt).test(t)) {
                    if (currValue)
                        return false;
                    currValue = true;
                }
            }
            return currValue;
        });
    }

    public boolean test(T obj) {
        return tester.test(obj);
    }
}
