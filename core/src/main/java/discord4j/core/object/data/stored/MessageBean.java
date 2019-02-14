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
package discord4j.core.object.data.stored;

import discord4j.common.json.MessageResponse;
import discord4j.common.json.ReactionResponse;
import discord4j.common.json.UserResponse;
import discord4j.core.object.data.stored.embed.EmbedBean;
import discord4j.gateway.json.dispatch.MessageCreate;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

public final class MessageBean implements Serializable {

    private static final long serialVersionUID = -3692990041360246588L;

    private long id;
    private long channelId;
    @Nullable
    private UserBean author;
    private String content;
    private String timestamp;
    @Nullable
    private String editedTimestamp;
    private boolean tts;
    private boolean mentionEveryone;
    private long[] mentions;
    private long[] mentionRoles;
    private AttachmentBean[] attachments;
    private EmbedBean[] embeds;
    @Nullable
    private ReactionBean[] reactions;
    private boolean pinned;
    @Nullable
    private Long webhookId;
    private int type;

    public MessageBean(final MessageResponse response) {
        id = response.getId();
        channelId = response.getChannelId();
        author = (response.getWebhookId() == null) ? new UserBean(response.getAuthor()) : null;
        content = response.getContent();
        timestamp = response.getTimestamp();
        editedTimestamp = response.getEditedTimestamp();
        tts = response.isTts();
        mentionEveryone = response.isMentionEveryone();

        mentions = Arrays.stream(response.getMentions())
                .mapToLong(UserResponse::getId)
                .toArray();

        mentionRoles = response.getMentionRoles();

        attachments = Arrays.stream(response.getAttachments())
                .map(AttachmentBean::new)
                .toArray(AttachmentBean[]::new);

        embeds = Arrays.stream(response.getEmbeds())
                .map(EmbedBean::new)
                .toArray(EmbedBean[]::new);

        final ReactionResponse[] reactions = response.getReactions();
        this.reactions = reactions == null ? null : Arrays.stream(reactions)
                .map(ReactionBean::new)
                .toArray(ReactionBean[]::new);

        pinned = response.isPinned();
        webhookId = response.getWebhookId();
        type = response.getType();
    }

    public MessageBean(MessageCreate messageCreate) {
        id = messageCreate.getId();
        channelId = messageCreate.getChannelId();
        author = (messageCreate.getWebhookId() == null) ? new UserBean(messageCreate.getAuthor()) : null;
        content = messageCreate.getContent();
        timestamp = messageCreate.getTimestamp();
        editedTimestamp = messageCreate.getEditedTimestamp();
        tts = messageCreate.isTts();
        mentionEveryone = messageCreate.isMentionEveryone();

        mentions = Arrays.stream(messageCreate.getMentions())
                .mapToLong(MessageCreate.Mention::getId)
                .toArray();

        mentionRoles = messageCreate.getMentionRoles();

        attachments = Arrays.stream(messageCreate.getAttachments())
                .map(AttachmentBean::new)
                .toArray(AttachmentBean[]::new);

        embeds = Arrays.stream(messageCreate.getEmbeds())
                .map(EmbedBean::new)
                .toArray(EmbedBean[]::new);

        this.reactions = null; // no reactions on message_create
        pinned = messageCreate.isPinned();
        webhookId = messageCreate.getWebhookId();
        type = messageCreate.getType();
    }

    public MessageBean(MessageBean toCopy) {
        id = toCopy.id;
        channelId = toCopy.channelId;
        author = toCopy.author;
        content = toCopy.content;
        timestamp = toCopy.timestamp;
        editedTimestamp = toCopy.editedTimestamp;
        tts = toCopy.tts;
        mentionEveryone = toCopy.mentionEveryone;
        mentions = toCopy.mentions;
        mentionRoles = toCopy.mentionRoles;
        attachments = toCopy.attachments;
        embeds = toCopy.embeds;
        reactions = toCopy.reactions;
        pinned = toCopy.pinned;
        webhookId = toCopy.webhookId;
        type = toCopy.type;
    }

    public MessageBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(final long channelId) {
        this.channelId = channelId;
    }

    @Nullable
    public UserBean getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable final UserBean author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    public void setEditedTimestamp(@Nullable final String editedTimestamp) {
        this.editedTimestamp = editedTimestamp;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(final boolean tts) {
        this.tts = tts;
    }

    public boolean isMentionEveryone() {
        return mentionEveryone;
    }

    public void setMentionEveryone(final boolean mentionEveryone) {
        this.mentionEveryone = mentionEveryone;
    }

    public long[] getMentions() {
        return mentions;
    }

    public void setMentions(final long[] mentions) {
        this.mentions = mentions;
    }

    public long[] getMentionRoles() {
        return mentionRoles;
    }

    public void setMentionRoles(final long[] mentionRoles) {
        this.mentionRoles = mentionRoles;
    }

    public AttachmentBean[] getAttachments() {
        return attachments;
    }

    public void setAttachments(final AttachmentBean[] attachments) {
        this.attachments = attachments;
    }

    public EmbedBean[] getEmbeds() {
        return embeds;
    }

    public void setEmbeds(final EmbedBean[] embeds) {
        this.embeds = embeds;
    }

    @Nullable
    public ReactionBean[] getReactions() {
        return reactions;
    }

    public void setReactions(@Nullable final ReactionBean[] reactions) {
        this.reactions = reactions;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(final boolean pinned) {
        this.pinned = pinned;
    }

    @Nullable
    public Long getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(@Nullable final Long webhookId) {
        this.webhookId = webhookId;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "id=" + id +
                ", channelId=" + channelId +
                ", author=" + author +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", editedTimestamp='" + editedTimestamp + '\'' +
                ", tts=" + tts +
                ", mentionEveryone=" + mentionEveryone +
                ", mentions=" + Arrays.toString(mentions) +
                ", mentionRoles=" + Arrays.toString(mentionRoles) +
                ", attachments=" + Arrays.toString(attachments) +
                ", embeds=" + Arrays.toString(embeds) +
                ", reactions=" + Arrays.toString(reactions) +
                ", pinned=" + pinned +
                ", webhookId=" + webhookId +
                ", type=" + type +
                '}';
    }
}
