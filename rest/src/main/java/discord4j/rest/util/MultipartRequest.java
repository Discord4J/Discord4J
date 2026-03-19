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
import org.jspecify.annotations.Nullable;
import reactor.netty.http.client.HttpClientForm;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MultipartRequest<T> {

    /**
     * Default file field (used for message files).
     *
     * @see <a href="https://docs.discord.com/developers/reference#uploading-files">Upload Files</a>
     */
    public static final String DEFAULT_FILE_FIELD = "files";

    private final @Nullable T jsonPayload;
    private final List<Tuple2<String, InputStream>> files;
    private final String fileField;
    private final Consumer<HttpClientForm> httpClientFormConsumer;

    private MultipartRequest(@Nullable T jsonPayload,
                             String fileField,
                             List<Tuple2<String, InputStream>> files) {
        this.jsonPayload = jsonPayload;
        this.files = Collections.unmodifiableList(files);
        this.fileField = fileField;
        this.httpClientFormConsumer = httpClientForm -> {
            for (int index = 0; index < this.getFiles().size(); index++) {
                // files format https://docs.discord.com/developers/reference#uploading-files
                final String name = this.getFileField().concat(String.format("[%d]", index));
                httpClientForm.file(
                        name,
                        this.getFiles().get(index).getT1(),
                        this.getFiles().get(index).getT2(),
                        "application/octet-stream"
                );
            }
        };
    }

    private MultipartRequest(@Nullable T jsonPayload,
                             String fileField,
                             Consumer<HttpClientForm> httpClientFormConsumer,
                             List<Tuple2<String, InputStream>> files) {
        this.jsonPayload = jsonPayload;
        this.files = Collections.unmodifiableList(files);
        this.fileField = fileField;
        this.httpClientFormConsumer = httpClientFormConsumer;
    }

    public static <T> MultipartRequest<T> ofEmpty() {
        return new MultipartRequest<>(null, DEFAULT_FILE_FIELD, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofEmptyRequest(String fileField) {
        return new MultipartRequest<>(null, fileField, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofRequest(T body,
                                                    String fileField) {
        return new MultipartRequest<>(body, fileField, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofRequest(T body) {
        return new MultipartRequest<>(body, DEFAULT_FILE_FIELD, Collections.emptyList());
    }

    public static <T> MultipartRequest<T> ofEmptyRequestAndFiles(List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(null, DEFAULT_FILE_FIELD, files);
    }

    public static <T> MultipartRequest<T> ofEmptyRequestAndFiles(String fileField,
                                                                 List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(null, fileField, files);
    }

    public static <T> MultipartRequest<T> ofRequestAndFiles(T body, List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(body, DEFAULT_FILE_FIELD, files);
    }

    public static <T> MultipartRequest<T> ofRequestAndFiles(T body,
                                                            String fileField,
                                                            List<Tuple2<String, InputStream>> files) {
        return new MultipartRequest<>(body, fileField, files);
    }

    @SuppressWarnings("unchecked")
    public <R> MultipartRequest<R> withFileHandler(MultipartFileHandler handler) {
        final Consumer<HttpClientForm> consumer = form ->
                handler.accept(form, this.files);

        return new MultipartRequest<>(
                (R) this.jsonPayload,
                this.fileField,
                consumer,
                this.files
        );
    }

    public <R> MultipartRequest<R> withRequest(R body) {
        return new MultipartRequest<>(body, DEFAULT_FILE_FIELD, this.files);
    }

    public MultipartRequest<T> addFile(String fileName,
                                       InputStream file) {
        List<Tuple2<String, InputStream>> list = new ArrayList<>(this.files);
        list.add(Tuples.of(fileName, file));
        List<Tuple2<String, InputStream>> unmodifiableList = Collections.unmodifiableList(list);
        if (this.jsonPayload == null) {
            return ofEmptyRequestAndFiles(this.fileField, unmodifiableList);
        }
        return ofRequestAndFiles(this.jsonPayload, this.fileField, unmodifiableList);
    }

    public MultipartRequest<T> addFiles(List<Tuple2<String, InputStream>> filesList) {
        List<Tuple2<String, InputStream>> list = new ArrayList<>(this.files);
        list.addAll(filesList);
        List<Tuple2<String, InputStream>> unmodifiableList = Collections.unmodifiableList(list);
        if (this.jsonPayload == null) {
            return ofEmptyRequestAndFiles(this.fileField, unmodifiableList);
        }
        return ofRequestAndFiles(this.jsonPayload, this.fileField, unmodifiableList);
    }

    /**
     * @deprecated Use {@link #getJsonPayload()} instead.
     */
    @Deprecated
    public @Nullable MessageCreateRequest getCreateRequest() {
        return (MessageCreateRequest) this.jsonPayload;
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

    public Consumer<HttpClientForm> getHttpClientFormConsumer() {
        return this.httpClientFormConsumer;
    }

}
