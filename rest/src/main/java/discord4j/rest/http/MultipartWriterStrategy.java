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
package discord4j.rest.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.rest.json.request.MessageCreateRequest;
import discord4j.rest.util.MultipartRequest;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientForm;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Write to a request from a {@code Consumer<HttpClientRequest.Form>} using reactor-netty's {@link
 * reactor.netty.http.client.HttpClient.RequestSender#sendForm(java.util.function.BiConsumer)}.
 *
 * @see HttpClientForm
 */
public class MultipartWriterStrategy implements WriterStrategy<MultipartRequest> {

    private static final Logger log = Loggers.getLogger(MultipartWriterStrategy.class);

    private final ObjectMapper objectMapper;

    public MultipartWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        return contentType != null && contentType.equals("multipart/form-data");
    }

    @Override
    public Mono<HttpClient.ResponseReceiver<?>> write(HttpClient.RequestSender send, @Nullable MultipartRequest body) {
        if (body == null) {
            return Mono.empty(); // or .error() ?
        }
        final MessageCreateRequest createRequest = body.getCreateRequest();
        return Mono.just(send.sendForm((request, form) -> {
            if (body.getFile() != null) {
                form.multipart(true).file("file", Optional.ofNullable(body.getFileName()).orElse("unknown"),
                        body.getFile(), "application/octet-stream");
            }
            if (createRequest != null) {
                try {
                    String payload = objectMapper.writeValueAsString(createRequest);
                    form.attr("payload_json", payload);
                } catch (JsonProcessingException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }));
    }
}
