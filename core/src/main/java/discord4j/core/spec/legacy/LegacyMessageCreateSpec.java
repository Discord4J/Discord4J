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
package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.component.LayoutComponent;
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
 * LegacySpec used to create {@link Message Messages} to {@link MessageChannel MessageChannels}. Clients using this spec must
 * have connected to gateway at least once.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#create-message">Create Message</a>
 */
public class LegacyMessageCreateSpec implements LegacySpec<MultipartRequest<MessageCreateRequest>> {

    @Nullable
    private String content;
    @Nullable
    private String nonce;
    private Boolean enforceNonce;
    private boolean tts;
    private List<EmbedData> embeds;
    private List<Tuple2<String, InputStream>> files;
    private AllowedMentionsData allowedMentionsData;
    private MessageReferenceData messageReferenceData;
    private List<LayoutComponent> components;

    /**
     * Sets the created {@link Message} contents, up to 2000 characters.
     *
     * @param content The message contents.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets a nonce that can be used for optimistic message sending.
     *
     * @param nonce An identifier.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setNonce(Snowflake nonce) {
        this.nonce = nonce.asString();
        return this;
    }

    /**
     * Sets if the nonce is enforced. If true and nonce is present, it will be checked for uniqueness in the past
     * few minutes. If another message was created by the same author with the same nonce, that message will be returned
     * and no new message will be created.
     *
     * @param enforceNonce Whether to enforce the nonce.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setEnforceNonce(boolean enforceNonce) {
        this.enforceNonce = enforceNonce;
        return this;
    }

    /**
     * Sets whether the created {@link Message} is a TTS message.
     *
     * @param tts If this message is a TTS message.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Sets rich content to the created {@link Message} in the form of an {@link Embed} object.
     * <p>
     * This method overrides any embeds added by {@link #addEmbed(Consumer)}.
     *
     * @param spec An {@link LegacyEmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     * @deprecated Use {@link #addEmbed(Consumer)}.
     */
    @Deprecated
    public LegacyMessageCreateSpec setEmbed(Consumer<? super LegacyEmbedCreateSpec> spec) {
        final LegacyEmbedCreateSpec mutatedSpec = new LegacyEmbedCreateSpec();
        spec.accept(mutatedSpec);
        embeds = Collections.singletonList(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Adds an embed to the message.
     * <p>
     * A message may have up to 10 embeds.
     *
     * @param spec An {@link LegacyEmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public LegacyMessageCreateSpec addEmbed(Consumer<? super LegacyEmbedCreateSpec> spec) {
        final LegacyEmbedCreateSpec mutatedSpec = new LegacyEmbedCreateSpec();
        spec.accept(mutatedSpec);
        if (embeds == null) {
            embeds = new ArrayList<>(1); // most common case is only 1 embed per message
        }
        embeds.add(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Adds a file as attachment to the created {@link Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public LegacyMessageCreateSpec addFile(String fileName, InputStream file) {
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
    public LegacyMessageCreateSpec addFileSpoiler(String fileName, InputStream file) {
        return addFile(Attachment.SPOILER_PREFIX + fileName, file);
    }

    /**
     * Sets an allowed mentions object to the message spec. Can be {@code null} to reset a configuration added by
     * default.
     *
     * @param allowedMentions The allowed mentions to add.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setAllowedMentions(@Nullable AllowedMentions allowedMentions) {
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
    public LegacyMessageCreateSpec setMessageReference(Snowflake messageId) {
        final LegacyMessageReferenceSpec spec = new LegacyMessageReferenceSpec();
        spec.setMessageId(messageId);
        messageReferenceData = spec.asRequest();
        return this;
    }

    /**
     * Sets the components of the message.
     *
     * @param components The message components.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setComponents(LayoutComponent... components) {
        return setComponents(Arrays.asList(components));
    }

    /**
     * Sets the components of the message.
     *
     * @param components The message components.
     * @return This spec.
     */
    public LegacyMessageCreateSpec setComponents(List<LayoutComponent> components) {
        this.components = components;
        return this;
    }

    @Override
    public MultipartRequest<MessageCreateRequest> asRequest() {
        MessageCreateRequest json = MessageCreateRequest.builder()
                .content(content == null ? Possible.absent() : Possible.of(content))
                .nonce(nonce == null ? Possible.absent() : Possible.of(nonce))
                .enforceNonce(enforceNonce == null ? Possible.absent() : Possible.of(enforceNonce))
                .tts(tts)
                .embeds(embeds == null ? Possible.absent() : Possible.of(embeds))
                .allowedMentions(allowedMentionsData == null ? Possible.absent() : Possible.of(allowedMentionsData))
                .messageReference(messageReferenceData == null ? Possible.absent() : Possible.of(messageReferenceData))
                .components(components == null ? Possible.absent() :
                        Possible.of(components.stream().map(LayoutComponent::getData).collect(Collectors.toList())))
                .build();
        return MultipartRequest.ofRequestAndFiles(json, files == null ? Collections.emptyList() : files);
    }
}
