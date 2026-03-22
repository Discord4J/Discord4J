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

import discord4j.common.util.Snowflake;
import org.immutables.value.Value;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@InlineFieldStyle
@Value.Enclosing
public class InviteCreateFields {

    public static final String TARGET_USERS_FILE_FIELD = "target_users_file";

    private InviteCreateFields() {
        throw new AssertionError();
    }

    @Value.Immutable
    public interface TargetUsersFile extends Spec<Tuple2<String, InputStream>> {

        String CONTENT_TYPE = "text/csv";

        static InviteCreateFields.TargetUsersFile of(List<Snowflake> ids) {
            final String dataTargetUsers = ids.stream()
                    .map(Snowflake::asString)
                    .collect(Collectors.joining(System.lineSeparator()));

            return of(new ByteArrayInputStream(dataTargetUsers.getBytes(StandardCharsets.UTF_8)));
        }

        static InviteCreateFields.TargetUsersFile of(InputStream inputStream) {
            return ImmutableInviteCreateFields.TargetUsersFile.of(inputStream);
        }

        default String name() {
            return "target_users.csv";
        }

        InputStream inputStream();

        @Override
        default Tuple2<String, InputStream> asRequest() {
            return Tuples.of(this.name(), this.inputStream());
        }
    }
}
