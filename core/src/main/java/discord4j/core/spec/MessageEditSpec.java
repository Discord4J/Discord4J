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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Spec used to edit {@link Message} entities this client has sent before.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#edit-message">Edit Message</a>
 */
public class MessageEditSpec implements Spec<MessageEditRequest>, Appendable {

    private final StringBuilder contentBuilder = new StringBuilder();
    private Possible<Optional<EmbedData>> embed = Possible.absent();
    private Possible<Optional<AllowedMentionsData>> allowedMentions = Possible.absent();
    private Possible<Integer> flags = Possible.absent();

    // Constants
    public final static byte MARKDOWN_ITALIC = 0;
    public final static byte MARKDOWN_BOLD = 1;
    public final static byte MARKDOWN_STRIKETHROUGH = 2;
    public final static byte MARKDOWN_UNDERLINE = 3;
    public final static byte MARKDOWN_CODELINE = 4;

    /**
     * Sets the new contents for the edited {@link Message}.
     *
     * @param content This message contents.
     * @return This spec.
     */
    public MessageEditSpec setContent(@Nullable String content) {
        this.contentBuilder.setLength(0);
        this.contentBuilder.append(content);
        return this;
    }

    /**
     * Gets the specs contents.
     *
     * @return Current contents of this spec.
     */
    public String getContent() {
        return contentBuilder.toString();
    }

    /**
     * Resets this specs contents.
     *
     * @return This spec.
     */
    public MessageEditSpec resetContent() {
        this.contentBuilder.setLength(0);
        return this;
    }

    /**
     * Adds a code block without a language to this spec.
     *
     * @param content Content of the code block.
     * @return This spec.
     */
    public MessageEditSpec appendCodeBlock(String content) {
        return appendCodeBlock(content, null);
    }

    /**
     * Adds a code block to this spec.
     *
     * @param content  The content of the code block.
     * @param language The language of the code block. (If null, no languages.)
     * @return This spec.
     */
    public MessageEditSpec appendCodeBlock(String content, @Nullable String language) {
        contentBuilder
            .append("```");
        if (language != null) {
            contentBuilder.append(language);
        }
        contentBuilder
            .append("\n")
            .append(content)
            .append("\n")
            .append("```\n");
        return this;
    }

    /**
     * Gets the content builder of this spec.
     * <b>Not recommended! Try to use {@link #getContent} and {@link #setContent} instead!</b>
     *
     * @return Content builder of this spec.
     */
    public StringBuilder getContentBuilder() {
        return contentBuilder;
    }

    /**
     * Adds formatted text to this spec.
     * @param content The content to be formatted and added.
     * @param formats {@link #MARKDOWN_ITALIC}, {@link #MARKDOWN_BOLD}, {@link #MARKDOWN_UNDERLINE} or {@link #MARKDOWN_CODELINE}
     * @return This spec.
     */
    public MessageEditSpec appendFormatted(String content, byte... formats) {
        ArrayList<String> tags = new ArrayList<>();
        for (byte format : formats) {
            String tag = getMarkdownTag(format);
            if (tag == null) {
                throw new IllegalArgumentException("Invalid markdown format!");
            }
            tags.add(tag);
        }
        tags.forEach(contentBuilder::append);
        contentBuilder.append(content);
        tags.forEach(contentBuilder::append);
        return this;
    }

    /**
     * Resets the embed.
     *
     * @return This spec.
     */
    public MessageEditSpec resetEmbed() {
        setEmbed(null);
        return this;
    }

    /**
     * Resets allowed mentions.
     *
     * @return This spec.
     */
    public MessageEditSpec resetAllowedMentions() {
        setAllowedMentions(null);
        return this;
    }

    /**
     * Resets this spec.
     *
     * @return This spec.
     */
    public MessageEditSpec reset() {
        return this
            .resetAllowedMentions()
            .resetContent()
            .resetEmbed()
            .resetFlags();
    }

    /**
     * Resets this specs files.
     *
     * @return This spec.
     */
    public MessageEditSpec resetFlags() {
        flags = Possible.absent();
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
    public MessageEditSpec setFlags(Message.Flag... flags) {
        this.flags = Possible.of(Arrays.stream(flags)
                .mapToInt(Message.Flag::getValue)
                .reduce(0, (left, right) -> left | right));
        return this;
    }

    @Override
    public MessageEditRequest asRequest() {
        return MessageEditRequest.builder()
                .content(contentBuilder.length() == 0 ? Possible.of(Optional.empty()) : Possible.of(Optional.of(contentBuilder.toString())))
                .embed(embed)
                .allowedMentions(allowedMentions)
                .flags(flags)
                .build();
    }

    @Override
    public Appendable append(CharSequence csq) {
        contentBuilder.append(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        contentBuilder.append(csq, start, end);
        return this;
    }

    @Override
    public Appendable append(char c) {
        contentBuilder.append(c);
        return this;
    }

    @Nullable
    private String getMarkdownTag(byte format) {
        switch(format) {
            case MARKDOWN_ITALIC:
                return "*";
            case MARKDOWN_BOLD:
                return "**";
            case MARKDOWN_UNDERLINE:
                return "__";
            case MARKDOWN_STRIKETHROUGH:
                return "~~";
            case MARKDOWN_CODELINE:
                return "`";
            default:
                return null;
        }
    }
}
