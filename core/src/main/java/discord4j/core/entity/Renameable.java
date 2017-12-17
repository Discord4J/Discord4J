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
package discord4j.core.entity;

import reactor.core.publisher.Mono;

/** A Discord object that can be renamed. */
public interface Renameable<T extends Renameable<T>> extends Nameable {

	/**
	 * Requests for this object to be renamed.
	 *
	 * @param name The new name for this object.
	 * @return A {@link Mono} where, upon successful completion, emits a <i>new</i> instance of itself; indicating the
	 * object was renamed. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<T> changeName(String name);
}
