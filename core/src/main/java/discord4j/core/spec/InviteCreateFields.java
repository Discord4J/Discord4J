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
package discord4j.core.spec;

import org.immutables.value.Value;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;

@InlineFieldStyle
@Value.Enclosing
public class InviteCreateFields {

    private InviteCreateFields() {
        throw new AssertionError();
    }

    @Value.Immutable
    public interface File extends Spec<Tuple2<String, InputStream>> {

        static InviteCreateFields.File of(String name, InputStream inputStream) {
            return ImmutableInviteCreateFields.File.of(name, inputStream);
        }

        String name();

        InputStream inputStream();

        @Override
        default Tuple2<String, InputStream> asRequest() {
            return Tuples.of(name(), inputStream());
        }
    }
}
