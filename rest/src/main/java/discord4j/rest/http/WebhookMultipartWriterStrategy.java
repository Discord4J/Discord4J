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
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.WebhookMultipartRequest;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientForm;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;

import java.io.InputStream;
import java.util.List;

/**
 * Write to a request from a {@code Consumer<HttpClientRequest.Form>} using reactor-netty's {@link
 * HttpClient.RequestSender#sendForm(java.util.function.BiConsumer)}.
 *
 * Note: This is a hold-over class to backport webhook execute functionality from 3.2.x
 *
 * @see HttpClientForm
 */
@Deprecated
public class WebhookMultipartWriterStrategy implements WriterStrategy<WebhookMultipartRequest> {

    private static final Logger log = Loggers.getLogger(WebhookMultipartWriterStrategy.class);

    private final ObjectMapper objectMapper;

    public WebhookMultipartWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        return contentType != null && contentType.equals("multipart/form-data") && type == null || type == WebhookMultipartRequest.class;
    }

    @Override
    public Mono<HttpClient.ResponseReceiver<?>> write(HttpClient.RequestSender send, @Nullable WebhookMultipartRequest body) {
        if (body == null) {
            return Mono.empty(); // or .error() ?
        }
        final WebhookExecuteRequest executeRequest = body.getExecuteRequest();
        final List<Tuple2<String, InputStream>> files = body.getFiles();
        return Mono.fromCallable(() -> send.sendForm((request, form) -> {
            form.multipart(true);
            if (body.getFiles().size() == 1) {
                form.file("file", files.get(0).getT1(), files.get(0).getT2(), "application/octet-stream");
            } else {
                for (int i = 0; i < files.size(); i++) {
                    form.file("file" + i, files.get(i).getT1(), files.get(i).getT2(), "application/octet-stream");
                }
            }

            if (executeRequest != null) {
                try {
                    String payload = objectMapper.writeValueAsString(executeRequest);
                    if (log.isTraceEnabled()) {
                        log.trace("{}", payload);
                    }
                    form.attr("payload_json", payload);
                } catch (JsonProcessingException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }));
    }
}
