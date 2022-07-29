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

package discord4j.common.store.legacy;

import java.util.*;
import java.util.function.Predicate;

class ListUtil {

    public static <T> /*~~>*/List<T> add(/*~~>*/List<T> source, T element) {
        /*~~>*/List<T> list = new ArrayList<>(source);
        list.add(element);
        return Collections.unmodifiableList(list);
    }

    public static <T> Collection<T> addAllDistinct(/*~~>*/List<T> source, /*~~>*/List<T> elements) {
        Set<T> set = new LinkedHashSet<>(source);
        set.addAll(elements);
        return Collections.unmodifiableCollection(set);
    }

    public static <T> /*~~>*/List<T> remove(/*~~>*/List<T> source, Predicate<T> filter) {
        if (source.isEmpty()) {
            return source;
        } else {
            /*~~>*/List<T> list = new ArrayList<>(source);
            list.removeIf(filter);
            return Collections.unmodifiableList(list);
        }
    }

    public static <T> /*~~>*/List<T> replace(/*~~>*/List<T> source, T old, T replacement) {
        /*~~>*/List<T> list = new ArrayList<>(source);
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i), old)) {
                list.set(i, replacement);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
