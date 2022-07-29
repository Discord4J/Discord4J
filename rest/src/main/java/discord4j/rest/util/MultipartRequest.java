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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipartRequest<T> {

    private final T jsonPayload;
    private final /*~~>*/List<Tuple2<String, InputStream>> files;

    private MultipartRequest(T jsonPayload, /*~~>*/List<Tuple2<String, InputStream>> files) {
        this.jsonPayload = jsonPayload;
        /*~~>*/this.files = Collections.unmodifiableList(files);
    }

    public static <T> MultipartRequest<T> ofRequest(T body) {
        return new MultipartRequest<>(body, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofRequestAndFiles(T body, /*~~>*/List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(body, files);
    }

    public <R> MultipartRequest<R> withRequest(R body) {
        return new MultipartRequest<>(body, files);
    }

    public MultipartRequest<T> addFile(String fileName, InputStream file) {
        /*~~>*/List<Tuple2<String, InputStream>> list = new ArrayList<>(/*~~>*/this.files);
        list.add(Tuples.of(fileName, file));
        return new MultipartRequest<>(this.jsonPayload, Collections.unmodifiableList(list));
    }

    public MultipartRequest<T> addFiles(/*~~>*/List<Tuple2<String, InputStream>> filesList) {
        /*~~>*/List<Tuple2<String, InputStream>> list = new ArrayList<>(/*~~>*/this.files);
        list.addAll(filesList);
        return new MultipartRequest<>(this.jsonPayload, Collections.unmodifiableList(list));
    }

    /**
     * @deprecated Use {@link #getJsonPayload()} instead.
     */
    @Deprecated
    @Nullable
    public MessageCreateRequest getCreateRequest() {
        return (MessageCreateRequest) jsonPayload;
    }

    public T getJsonPayload() { return jsonPayload; }

    public /*~~>*/List<Tuple2<String, InputStream>> getFiles() {
        return files;
    }
}
