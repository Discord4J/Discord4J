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

import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import discord4j.rest.util.Permission;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Spec used to create {@link Message Messages} to {@link MessageChannel MessageChannels}. Clients using this spec must
 * have connected to gateway at least once.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#create-message">Create Message</a>
 */
public class MessageCreateSpec implements Spec<MultipartRequest<MessageCreateRequest>> {

    @Nullable
    private String content;
    @Nullable
    private String nonce;
    private boolean tts;
    private EmbedData embed;
    private List<Tuple2<String, InputStream>> files;
    private AllowedMentionsData allowedMentionsData;
    private MessageReferenceData messageReferenceData;
    private List<ActionRow> actionRows;

    /**
     * Sets the created {@link Message} contents, up to 2000 characters.
     *
     * @param content The message contents.
     * @return This spec.
     */
    public MessageCreateSpec setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets a nonce that can be used for optimistic message sending.
     *
     * @param nonce An identifier.
     * @return This spec.
     */
    public MessageCreateSpec setNonce(Snowflake nonce) {
        this.nonce = nonce.asString();
        return this;
    }

    /**
     * Sets whether the created {@link Message} is a TTS message.
     *
     * @param tts If this message is a TTS message.
     * @return This spec.
     */
    public MessageCreateSpec setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Sets rich content to the created {@link Message} in the form of an {@link Embed} object.
     *
     * @param spec An {@link EmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public MessageCreateSpec setEmbed(Consumer<? super EmbedCreateSpec> spec) {
        final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
        spec.accept(mutatedSpec);
        embed = mutatedSpec.asRequest();
        return this;
    }

    /**
     * Adds a file as attachment to the created {@link Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public MessageCreateSpec addFile(String fileName, InputStream file) {
        if (files == null) {
            files = new ArrayList<>(1); // most common case is only 1 attachment per message
        }
        files.add(Tuples.of(fileName, file));
        return this;
    }

    /**
     * Adds a spoiler file as attachment to the created {@link Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public MessageCreateSpec addFileSpoiler(String fileName, InputStream file) {
        return addFile(Attachment.SPOILER_PREFIX + fileName, file);
    }

    /**
     * Sets an allowed mentions object to the message spec. Can be {@code null} to reset a configuration added by
     * default.
     *
     * @param allowedMentions The allowed mentions to add.
     * @return This spec.
     */
    public MessageCreateSpec setAllowedMentions(@Nullable AllowedMentions allowedMentions) {
        allowedMentionsData = allowedMentions != null ? allowedMentions.toData() : null;
        return this;
    }

    /**
     * Adds a message ID to reply to. This requires the {@link Permission#READ_MESSAGE_HISTORY} permission, and the
     * referenced message must exist and cannot be a system message.
     *
     * @param messageId The ID of the message to reply to.
     * @return This spec.
     */
    public MessageCreateSpec setMessageReference(Snowflake messageId) {
        final MessageReferenceSpec spec = new MessageReferenceSpec();
        spec.setMessageId(messageId);
        messageReferenceData = spec.asRequest();
        return this;
    }

    public MessageCreateSpec setActionRows(List<ActionRow> actionRows) {
        this.actionRows = actionRows;
        return this;
    }

    public MessageCreateSpec setActionRows(ActionRow... actionRows) {
        return setActionRows(Arrays.asList(actionRows));
    }

    @Override
    public MultipartRequest<MessageCreateRequest> asRequest() {
        MessageCreateRequest json = MessageCreateRequest.builder()
                .content(content == null ? Possible.absent() : Possible.of(content))
                .nonce(nonce == null ? Possible.absent() : Possible.of(nonce))
                .tts(tts)
                .embed(embed == null ? Possible.absent() : Possible.of(embed))
                .allowedMentions(allowedMentionsData == null ? Possible.absent() : Possible.of(allowedMentionsData))
                .messageReference(messageReferenceData == null ? Possible.absent() : Possible.of(messageReferenceData))
                .components(actionRows.stream().map(ActionRow::getData).collect(Collectors.toList()))
                .build();
        return MultipartRequest.ofRequestAndFiles(json, files == null ? Collections.emptyList() : files);
    }
}
