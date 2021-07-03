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

import java.util.*;
import java.util.function.Consumer;

/**
 * Spec used to edit {@link Message} entities this client has sent before.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#edit-message">Edit Message</a>
 */
public class MessageEditSpec implements Spec<MessageEditRequest> {

    private Possible<Optional<String>> content = Possible.absent();
    private Possible<Optional<List<EmbedData>>> embeds = Possible.absent();
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
     * @deprecated Use {@link #addEmbed(Consumer)} or {@link #removeEmbeds()}
     */
    @Deprecated
    public MessageEditSpec setEmbed(@Nullable Consumer<? super EmbedCreateSpec> spec) {
        if (spec != null) {
            final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
            spec.accept(mutatedSpec);
            this.embeds = Possible.of(Optional.of(Collections.singletonList(mutatedSpec.asRequest())));
        } else {
            this.embeds = Possible.of(Optional.empty());
        }

        return this;
    }

    /**
     * Adds an embed to the edit request.
     * <p>
     * <b>Warning:</b> This method does <i>not</i> add an embed to the embeds already existing on the message. That is,
     * if a message has embeds A and B, editing it with {@code addEmbed(C)} will result in the message having <i>only</i>
     * embed C. To actually add embed C to the message, all embeds must be sent
     * (i.e., do {@code addEmbed(A).addEmbed(B).addEmbed(C)}.
     *
     * @param spec An {@link EmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public MessageEditSpec addEmbed(Consumer<? super EmbedCreateSpec> spec) {
        final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
        spec.accept(mutatedSpec);

        // if the Possible or the Optional is empty
        if (this.embeds.isAbsent() || !this.embeds.get().isPresent()) {
            this.embeds = Possible.of(Optional.of(new ArrayList<>(1)));
        }

        this.embeds.get().get().add(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Removes all of the embeds on the message.
     *
     * @return This spec.
     */
    public MessageEditSpec removeEmbeds() {
        this.embeds = Possible.of(Optional.empty());
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
                .embeds(embeds)
                .allowedMentions(allowedMentions)
                .flags(flags)
                .build();
    }
}
