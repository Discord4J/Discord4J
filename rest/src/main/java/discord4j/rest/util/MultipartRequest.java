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

package discord4j.rest.util;

import discord4j.discordjson.json.MessageCreateRequest;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MultipartRequest {

    private final MessageCreateRequest createRequest;
    private final List<Tuple2<String, InputStream>> files;

    public MultipartRequest(MessageCreateRequest createRequest) {
        this(createRequest, Collections.emptyList());
    }

    public MultipartRequest(MessageCreateRequest createRequest, String fileName, InputStream file) {
        this(createRequest, Collections.singletonList(Tuples.of(fileName, file)));
    }

    public MultipartRequest(MessageCreateRequest createRequest, List<Tuple2<String, InputStream>> files) {
        this.createRequest = createRequest;
        this.files = files;
    }

    @Nullable
    public MessageCreateRequest getCreateRequest() {
        return createRequest;
    }

    public List<Tuple2<String, InputStream>> getFiles() {
        return files;
    }
}
