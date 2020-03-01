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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.util;

import discord4j.discordjson.possible.Possible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ListUtil {

    public static <T> Possible<List<T>> add(Possible<List<T>> source, T element) {
        if (source.isAbsent()) {
            return Possible.of(Collections.singletonList(element));
        } else {
            List<T> list = new ArrayList<>(source.get());
            list.add(element);
            return Possible.of(Collections.unmodifiableList(list));
        }
    }

    public static <T> Possible<List<T>> addAll(Possible<List<T>> source, List<T> elements) {
        if (source.isAbsent()) {
            return Possible.of(Collections.unmodifiableList(elements));
        } else {
            List<T> list = new ArrayList<>(source.get());
            list.addAll(elements);
            return Possible.of(Collections.unmodifiableList(list));
        }
    }

    public static <T> Possible<List<T>> remove(Possible<List<T>> source, Predicate<T> filter) {
        if (source.isAbsent()) {
            return source;
        } else {
            List<T> list = new ArrayList<>(source.get());
            list.removeIf(filter);
            return Possible.of(Collections.unmodifiableList(list));
        }
    }

    public static <T> List<T> remove(List<T> source, Predicate<T> filter) {
        if (source.isEmpty()) {
            return source;
        } else {
            List<T> list = new ArrayList<>(source);
            list.removeIf(filter);
            return Collections.unmodifiableList(list);
        }
    }

    public static <T> Possible<List<T>> replace(Possible<List<T>> source, T old, T replacement) {
        if (source.isAbsent()) {
            return source;
        } else {
            List<T> list = new ArrayList<>(source.get());
            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(list.get(i), old)) {
                    list.set(i, replacement);
                }
            }
            return Possible.of(Collections.unmodifiableList(list));
        }
    }
}
