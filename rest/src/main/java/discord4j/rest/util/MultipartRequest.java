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

    /**
     * Default file field (used for message creation).
     */
    private static final String FILE_FIELD = "file";

    private final @Nullable T jsonPayload;
    private final List<Tuple2<String, InputStream>> files;
    private final String fileField;

    private MultipartRequest(@Nullable T jsonPayload, String fileField, List<Tuple2<String, InputStream>> files) {
        this.jsonPayload = jsonPayload;
        this.files = Collections.unmodifiableList(files);
        this.fileField = fileField;
    }

    public static <T> MultipartRequest<T> ofEmptyRequest(String fileField) {
        return new MultipartRequest<>(null, fileField, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofRequest(T body, String fileField) {
        return new MultipartRequest<>(body, fileField, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofRequest(T body) {
        return new MultipartRequest<>(body, FILE_FIELD, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofEmptyRequestAndFiles(List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(null, FILE_FIELD, files);
    }

    public static <T> MultipartRequest<T> ofEmptyRequestAndFiles(String fileField, List<Tuple2<String,
        InputStream>> files) {
        return new MultipartRequest<>(null, fileField, files);
    }

    public static <T> MultipartRequest<T> ofRequestAndFiles(T body, List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(body, FILE_FIELD, files);
    }

    public static <T> MultipartRequest<T> ofRequestAndFiles(T body, String fileField, List<Tuple2<String,
        InputStream>> files) {
        return new MultipartRequest<>(body, fileField, files);
    }

    public <R> MultipartRequest<R> withRequest(R body) {
        return new MultipartRequest<>(body, FILE_FIELD, files);
    }

    public MultipartRequest<T> addFile(String fileName, InputStream file) {
        List<Tuple2<String, InputStream>> list = new ArrayList<>(this.files);
        list.add(Tuples.of(fileName, file));
        return new MultipartRequest<>(this.jsonPayload, this.fileField, Collections.unmodifiableList(list));
    }

    public MultipartRequest<T> addFiles(List<Tuple2<String, InputStream>> filesList) {
        List<Tuple2<String, InputStream>> list = new ArrayList<>(this.files);
        list.addAll(filesList);
        return new MultipartRequest<>(this.jsonPayload, this.fileField, Collections.unmodifiableList(list));
    }

    /**
     * @deprecated Use {@link #getJsonPayload()} instead.
     */
    @Deprecated
    @Nullable
    public MessageCreateRequest getCreateRequest() {
        return (MessageCreateRequest) jsonPayload;
    }

    public @Nullable T getJsonPayload() {
        return this.jsonPayload;
    }

    public String getFileField() {
        return this.fileField;
    }

    public List<Tuple2<String, InputStream>> getFiles() {
        return this.files;
    }
}
