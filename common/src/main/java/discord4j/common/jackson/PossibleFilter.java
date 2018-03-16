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
package discord4j.common.jackson;

public class PossibleFilter {

    @Override
    public boolean equals(Object obj) {
        // Nulls should be included. Only exclude values which are Possible.absent
        if (obj == null) {
            return false;
        } else if (Possible.class.isAssignableFrom(obj.getClass())) {
            return ((Possible) obj).isAbsent();
        } else if (PossibleLong.class.isAssignableFrom(obj.getClass())) {
            return ((PossibleLong) obj).isAbsent();
        }

        return false;
    }
}
