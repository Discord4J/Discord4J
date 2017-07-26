/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util.cache;

import com.koloboke.collect.set.LongSet;
import com.koloboke.function.LongObjConsumer;
import com.koloboke.function.LongObjFunction;
import com.koloboke.function.LongObjPredicate;
import sx.blah.discord.handle.obj.IIDLinkedObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A backing object an {@link Cache}.
 *
 * <p>Implementors <b>must</b> be thread safe.
 */
public interface ICacheDelegate<T extends IIDLinkedObject> extends RandomAccess, Iterable<T> {

	/**
	 * Gets an object by its unique snowflake ID.
	 *
	 * @param id The ID of the desired object.
	 * @return The object with the provided ID (or null if one was not found).
	 */
	default Optional<T> retrieve(String id) {
		return retrieve(Long.parseUnsignedLong(id));
	}

	/**
	 * Gets an object by its unique snowflake ID.
	 *
	 * @param id The ID of the desired object.
	 * @return The object with the provided ID (or null if one was not found).
	 */
	Optional<T> retrieve(long id);

	/**
	 * Puts an object into the cache.
	 *
	 * @param obj The object to put.
	 * @return The previous object that had the same ID or null if there was not one.
	 */
	Optional<T> put(T obj);

	/**
	 * Puts every element of the given cache into the cache.
	 *
	 * @param objs The objects to insert.
	 * @return Any objects that were replaced by the operation.
	 */
	default Collection<T> putAll(Collection<T> objs) {
		return objs.stream().map(obj -> put(obj).orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	/**
	 * Removes an object from the cache.
	 *
	 * @param id The ID of the object to remove.
	 * @return The object that was removed.
	 */
	default Optional<T> remove(String id) {
		return remove(Long.parseUnsignedLong(id));
	}

	/**
	 * Removes an object from the cache.
	 *
	 * @param id The ID of the object to remove.
	 * @return The object that was removed.
	 */
	Optional<T> remove(long id);

	/**
	 * Removes an object from the cache.
	 *
	 * @param obj The object to remove.
	 * @return The object that was removed.
	 */
	default Optional<T> remove(T obj) {
		return remove(obj.getLongID());
	}

	/**
	 * Clears the cache.
	 *
	 * @return The cache.
	 */
	Collection<T> clear();

	/**
	 * Gets whether the given key is present in the cache.
	 *
	 * @param id The key to search for.
	 * @return Whether the given key is present in the cache.
	 */
	default boolean contains(String id) {
		return contains(Long.parseUnsignedLong(id));
	}

	/**
	 * Gets whether the given key is present in the cache.
	 *
	 * @param id The key to search for.
	 * @return Whether the given key is present in the cache.
	 */
	default boolean contains(long id) {
		return retrieve(id).isPresent();
	}

	/**
	 * Gets whether the given value is present in the cache.
	 *
	 * @param obj The value to search for.
	 * @return Whether the given value is present in the cache.
	 */
	default boolean contains(T obj) {
		return contains(obj.getLongID());
	}

	/**
	 * Gets the number of elements stored in the cache.
	 *
	 * @return The number of elements stored in the cache.
	 */
	int size();

	/**
	 * Gets the IDs of every object in the cache.
	 *
	 * @return The IDs of every object in the cache.
	 */
	default Collection<String> ids() {
		return longIDs().stream().map(Long::toUnsignedString).collect(Collectors.toSet());
	}

	/**
	 * Gets the IDs of every object in the cache.
	 *
	 * @return The IDs of every object in the cache.
	 */
	LongSet longIDs();

	/**
	 * Gets every value in the cache.
	 *
	 * @return Every value in the cache.
	 */
	Collection<T> values();

	/**
	 * Gets a copy of the cache.
	 *
	 * @return A copy of the cache.
	 */
	ICacheDelegate<T> copy();

	/**
	 * Gets a copy of the cache as a long map.
	 *
	 * @return A copy of the cache as a long map.
	 */
	LongMap<T> mapCopy();

	/**
	 * Performs the given action for each pair of key and value in the cache.
	 *
	 * @param action The action to perform for each pair of key and value in the cache.
	 */
	void forEach(LongObjConsumer<? super T> action);

	/**
	 * Performs the given action for each pair of key and value in the cache while the function returns true.
	 *
	 * @param predicate The action to perform for each pair of key and value in the cache.
	 * @return Whether iterating was interrupted (whether the predicate ever returned false).
	 */
	boolean forEachWhile(LongObjPredicate<? super T> predicate);

	/**
	 * Gets the first non-null value produced by the given function which is applied to every pair of keys and values
	 * in the cache.
	 *
	 * @param function The function to apply to each pair.
	 * @return The first non-null value produced by the given function
	 */
	<Z> Z findResult(LongObjFunction<? super T, ? extends Z> function);

	@Override
	default Spliterator<T> spliterator() {
		return Spliterators.spliterator(values(), 0);
	}

	/**
	 * See {@link Collection#stream()}.
	 */
	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * See {@link Collection#parallelStream()}.
	 */
	default Stream<T> parallelStream() {
		return StreamSupport.stream(spliterator(), true);
	}
}
