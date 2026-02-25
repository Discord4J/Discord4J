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
package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable(singleton = true)
public interface InviteTargetUsersUpdateSpecGenerator extends Spec<Void> {

    List<Snowflake> targetUserIds();

    @Override
    default Void asRequest() {
        return null;
    }

    default MultipartRequest<Void> asMultipartRequest() {
        final String dataTargetUsers = targetUserIds().stream().map(Snowflake::asString).collect(Collectors.joining(System.lineSeparator()));
        MultipartRequest<Void> inviteMultipartRequest = MultipartRequest.ofEmptyRequest("target_users_file");
        InviteCreateFields.File file = InviteCreateFields.File.of("target_users_file", new ByteArrayInputStream(dataTargetUsers.getBytes(StandardCharsets.UTF_8)));
        return inviteMultipartRequest.addFile(file.name(), file.inputStream());
    }

}
