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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * LegacySpec used to create {@link discord4j.core.object.entity.Message Messages} via a {@link discord4j.core.object.entity.Webhook}.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook#execute-webhook">Execute webhook</a>
 */
public class LegacyWebhookExecuteSpec implements LegacySpec<MultipartRequest<WebhookExecuteRequest>> {

    private Possible<String> content = Possible.absent();
    private Possible<String> username = Possible.absent();
    private Possible<String> avatarUrl = Possible.absent();
    private Possible<Boolean> tts = Possible.absent();
    private List<Tuple2<String, InputStream>> files = null;
    private List<EmbedData> embeds = null;
    private Possible<AllowedMentionsData> allowedMentions = Possible.absent();
    private Possible<Snowflake> threadId = Possible.absent();

    /**
     * Sets the created {@link discord4j.core.object.entity.Message} contents, up to 2000 characters.
     *
     * @param content The message contents.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setContent(String content) {
        this.content = Possible.of(content);
        return this;
    }

    /**
     * Sets a username that overrides the default username of the {@link discord4j.core.object.entity.Webhook}.
     *
     * @param username The webhook username.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setUsername(String username) {
        this.username = Possible.of(username);
        return this;
    }

    /**
     * Sets an avatar that overrides the default avatar of the {@link discord4j.core.object.entity.Webhook}.
     *
     * @param avatarUrl The url to the avatar.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setAvatarUrl(String avatarUrl) {
        this.avatarUrl = Possible.of(avatarUrl);
        return this;
    }

    /**
     * Sets whether the created {@link discord4j.core.object.entity.Message} is a TTS message.
     *
     * @param tts If created message is a TTS message.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setTts(boolean tts) {
        this.tts = Possible.of(tts);
        return this;
    }

    /**
     * Adds a file as attachment to the created {@link discord4j.core.object.entity.Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec addFile(String fileName, InputStream file) {
        if (this.files == null) {
            this.files = new ArrayList<>(1); // most likely only one file.
        }
        this.files.add(Tuples.of(fileName, file));
        return this;
    }

    /**
     * Adds a spoiler file as attachment to the created {@link discord4j.core.object.entity.Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec addFileSpoiler(String fileName, InputStream file) {
        return addFile(Attachment.SPOILER_PREFIX + fileName, file);
    }

    /**
     * Adds rich content to the created {@link discord4j.core.object.entity.Message} in the form of an {@link discord4j.core.object.Embed} object.
     *
     * @param spec An {@link LegacyEmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec addEmbed(Consumer<? super LegacyEmbedCreateSpec> spec) {
        final LegacyEmbedCreateSpec mutatedSpec = new LegacyEmbedCreateSpec();
        spec.accept(mutatedSpec);
        if (this.embeds == null) {
            this.embeds = new ArrayList<>(1); // Most likely only one embed will be specified.
        }
        this.embeds.add(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Adds an allowed mentions object to the webhook execute spec.
     * @param allowedMentions The allowed mentions to add.
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setAllowedMentions(AllowedMentions allowedMentions) {
        this.allowedMentions = Possible.of(allowedMentions.toData());
        return this;
    }

    /**
     * Set the thread id within the webhook's channel. The thread will automatically be unarchived.
     *
     * @param threadId the thread id to set
     * @return This spec.
     */
    public LegacyWebhookExecuteSpec setThreadId(Snowflake threadId) {
        this.threadId = Possible.of(threadId);
        return this;
    }

    /**
     * Get the thread id within the webhook's channel.
     *
     * @return The thread id.
     */
    public Possible<Snowflake> getThreadId() {
        return this.threadId;
    }

    @Override
    public MultipartRequest<WebhookExecuteRequest> asRequest() {
        return MultipartRequest.ofRequestAndFiles(
                WebhookExecuteRequest
                        .builder()
                        .content(content)
                        .username(username)
                        .avatarUrl(avatarUrl)
                        .tts(tts)
                        .embeds(embeds == null ? Possible.absent() : Possible.of(embeds))
                        .allowedMentions(allowedMentions)
                        .build(),
                files == null ? Collections.emptyList() : files
        );
    }
}
