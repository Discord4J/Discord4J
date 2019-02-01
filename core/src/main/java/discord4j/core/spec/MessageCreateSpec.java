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

import discord4j.core.object.util.Snowflake;
import discord4j.rest.json.request.EmbedRequest;
import discord4j.rest.json.request.MessageCreateRequest;
import discord4j.rest.util.MultipartRequest;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MessageCreateSpec implements Spec<MultipartRequest> {

    @Nullable
    private String content;
    @Nullable
    private String nonce;
    private boolean tts;
    private EmbedRequest embed;
    private List<Tuple2<String, InputStream>> files;

    public MessageCreateSpec setContent(String content) {
        this.content = content;
        return this;
    }

    public MessageCreateSpec setNonce(Snowflake nonce) {
        this.nonce = nonce.asString();
        return this;
    }

    public MessageCreateSpec setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    public MessageCreateSpec setEmbed(Consumer<? super EmbedCreateSpec> spec) {
        final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
        spec.accept(mutatedSpec);
        embed = mutatedSpec.asRequest();
        return this;
    }

    public MessageCreateSpec addFile(String fileName, InputStream file) {
        if (files == null) files = new ArrayList<>(1);
        files.add(Tuples.of(fileName, file));
        return this;
    }

    @Override
    public MultipartRequest asRequest() {
        MessageCreateRequest json = new MessageCreateRequest(content, nonce, tts, embed);
        return new MultipartRequest(json, files == null ? Collections.emptyList() : files);
    }
}
