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

import java.util.List;

@Value.Immutable(singleton = true)
public interface InviteTargetUsersUpdateSpecGenerator extends Spec<Void> {

    List<Snowflake> targetUserIds();

    @Override
    default Void asRequest() {
        return null;
    }

    default MultipartRequest<Void> asMultipartRequest() {
        MultipartRequest<Void> multipartRequest =
                MultipartRequest.ofEmptyRequest(InviteCreateFields.TARGET_USERS_FILE_FIELD);

        final InviteCreateFields.TargetUsersFile file = InviteCreateFields.TargetUsersFile.of(this.targetUserIds());

        multipartRequest = multipartRequest.addFile(file.name(), file.inputStream());
        multipartRequest = multipartRequest.withFileHandler((form, files) ->
                form.file(
                        InviteCreateFields.TARGET_USERS_FILE_FIELD,
                        files.get(0).getT1(),
                        files.get(0).getT2(),
                        InviteCreateFields.TargetUsersFile.CONTENT_TYPE
                )
        );
        return multipartRequest;
    }

}
