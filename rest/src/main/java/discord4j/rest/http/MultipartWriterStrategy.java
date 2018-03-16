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
import discord4j.common.json.request.MessageCreateRequest;
import discord4j.rest.util.MultipartRequest;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Write to a request from a {@code Consumer<HttpClientRequest.Form>} using reactor-netty's {@link
 * HttpClientRequest#sendForm(java.util.function.Consumer)}.
 *
 * @see HttpClientRequest.Form
 */
public class MultipartWriterStrategy implements WriterStrategy<MultipartRequest> {

    private final ObjectMapper objectMapper;

    public MultipartWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        return contentType != null && contentType.equals("multipart/form-data");
    }

    @Override
    public Mono<Void> write(HttpClientRequest request, @Nullable MultipartRequest body) {
        if (body == null) {
            return Mono.empty(); // or .error() ?
        }
        Consumer<HttpClientRequest.Form> formConsumer = body.getFormConsumer();
        MessageCreateRequest createRequest = body.getCreateRequest();
        if (createRequest != null) {
            try {
                String payload = objectMapper.writeValueAsString(createRequest);
                formConsumer = formConsumer.andThen(form -> form.attr("payload_json", payload));
            } catch (JsonProcessingException e) {
                throw Exceptions.propagate(e);
            }
        }
        return request.chunkedTransfer(false).sendForm(formConsumer).then();
    }
}
