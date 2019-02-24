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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class ArrayUtil {

    public static <T> T[] remove(T[] array, T t) {
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(t)) {
                System.arraycopy(array, 0, ret, 0, i);
                if (i < array.length - 1) {
                    System.arraycopy(array, i + 1, ret, i, array.length - i - 1);
                }
                break;
            }
        }
        return ret;
    }

    public static long[] remove(long[] array, long l) {
        long[] ret = new long[Math.max(0, array.length - 1)];
        for (int i = 0; i < array.length; i++) {
            if (array[i] == l) {
                System.arraycopy(array, 0, ret, 0, i);
                if (i < array.length - 1) {
                    System.arraycopy(array, i + 1, ret, i, array.length - i - 1);
                }
                break;
            }
        }
        return ret;
    }

    public static <T> T[] add(T[] array, T t) {
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, ret, 0, ret.length - 1);
        ret[ret.length - 1] = t;
        return ret;
    }

    public static long[] add(long[] array, long l) {
        long[] ret = new long[array.length + 1];
        System.arraycopy(array, 0, ret, 0, ret.length - 1);
        ret[ret.length - 1] = l;
        return ret;
    }

    public static long[] addAll(long[] array0, long[] array1) {
        long[] ret = new long[array0.length + array1.length];
        System.arraycopy(array0, 0, ret, 0, array0.length);
        System.arraycopy(array1, 0, ret, array0.length, array1.length);
        return ret;
    }

    public static Long[] toObject(long[] array) {
        Long[] ret = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            ret[i] = array[i];
        }
        return ret;
    }

    public static <T> T[] replace(T[] array, T old, T replacement) {
        T[] copy = Arrays.copyOf(array, array.length);
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], old)) {
                copy[i] = replacement;
            }
        }
        return copy;
    }

    public static boolean contains(long[] array, long l) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == l) {
                return true;
            }
        }
        return false;
    }
}
