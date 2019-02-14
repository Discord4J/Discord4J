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

import discord4j.common.jackson.Possible;
import discord4j.rest.json.request.EmbedRequest;
import discord4j.rest.json.request.MessageEditRequest;
import reactor.util.annotation.Nullable;

import java.util.function.Consumer;

public class MessageEditSpec implements Spec<MessageEditRequest> {

    @Nullable
    private Possible<String> content = Possible.absent();
    @Nullable
    private Possible<EmbedRequest> embed = Possible.absent();

    public MessageEditSpec setContent(@Nullable String content) {
        this.content = content == null ? null : Possible.of(content);
        return this;
    }

    public MessageEditSpec setEmbed(@Nullable Consumer<? super EmbedCreateSpec> spec) {
        if (spec != null) {
            final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
            spec.accept(mutatedSpec);
            embed = Possible.of(mutatedSpec.asRequest());
        } else {
            embed = null;
        }

        return this;
    }

    @Override
    public MessageEditRequest asRequest() {
        return new MessageEditRequest(content, embed);
    }
}
