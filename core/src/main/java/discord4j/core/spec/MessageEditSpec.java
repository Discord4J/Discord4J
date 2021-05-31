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

import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Spec used to edit {@link Message} entities this client has sent before.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#edit-message">Edit Message</a>
 */
public class MessageEditSpec implements Spec<MessageEditRequest> {

    private Possible<Optional<String>> content = Possible.absent();
    private Possible<Optional<EmbedData>> embed = Possible.absent();
    private Possible<Optional<AllowedMentionsData>> allowedMentions = Possible.absent();
    private Possible<Optional<Integer>> flags = Possible.absent();

    /**
     * Sets the new contents for the edited {@link Message}.
     *
     * @param content This message contents.
     * @return This spec.
     */
    public MessageEditSpec setContent(@Nullable String content) {
        this.content = Possible.of(Optional.ofNullable(content));
        return this;
    }

    /**
     * Sets the new rich content for the edited {@link Message}.
     *
     * @param spec An {@link EmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public MessageEditSpec setEmbed(@Nullable Consumer<? super EmbedCreateSpec> spec) {
        if (spec != null) {
            final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
            spec.accept(mutatedSpec);
            this.embed = Possible.of(Optional.of(mutatedSpec.asRequest()));
        } else {
            this.embed = Possible.of(Optional.empty());
        }

        return this;
    }

    /**
     * Sets the new allowed mentions for the edited {@link Message}.
     *
     * @param allowedMentions This message allowed mentions.
     * @return This spec.
     */
    public MessageEditSpec setAllowedMentions(@Nullable AllowedMentions allowedMentions) {
        this.allowedMentions = Possible.of(Optional.ofNullable(allowedMentions).map(AllowedMentions::toData));
        return this;
    }

    /**
     * Sets the flags for the edited {@link Message}.
     *
     * @param flags An array of {@link Message.Flag} to set on the edited message.
     * @return This spec.
     */
    public MessageEditSpec setFlags(@Nullable Message.Flag... flags) {
        if (flags != null) {
            this.flags = Possible.of(Optional.of(Arrays.stream(flags)
                    .mapToInt(Message.Flag::getValue)
                    .reduce(0, (left, right) -> left | right)));
        } else {
            this.flags = Possible.of(Optional.empty());
        }
        return this;
    }

    @Override
    public MessageEditRequest asRequest() {
        return MessageEditRequest.builder()
                .content(content)
                .embed(embed)
                .allowedMentions(allowedMentions)
                .flags(flags)
                .build();
    }
}
