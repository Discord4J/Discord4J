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
package discord4j.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface HelpPage {

    String getName();

    default List<String> getAlternativeNames() {
        return Collections.emptyList();
    }

    default Optional<String> getSynopsis() {
        return Optional.empty();
    }

    default Optional<String> getDescription() {
        return Optional.empty();
    }

    default List<String> getExamples() {
        return Collections.emptyList();
    }

    default Map<String, String> getFields() {
        return Collections.emptyMap();
    }

    default List<HelpPage> getRelatedPages() {
        return Collections.emptyList();
    }
}
