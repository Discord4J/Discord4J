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

import discord4j.discordjson.json.MessageRequestBase;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface FilesMessageRequestSpec<T extends MessageRequestBase> extends MessageRequestSpec<MultipartRequest<T>> {

    @Value.Default
    default List<MessageCreateFields.File> files() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<MessageCreateFields.FileSpoiler> fileSpoilers() {
        return Collections.emptyList();
    }

    T getMessageRequest();

    @Override
    default MultipartRequest<T> asRequest() {
        return MultipartRequest.ofRequestAndFiles(getMessageRequest(), Stream.concat(files().stream(), fileSpoilers().stream())
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList()));
    }
}
