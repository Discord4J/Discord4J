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

package discord4j.rest.entity.data;

import discord4j.common.json.MessageResponse;
import discord4j.common.json.ReactionResponse;
import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class MessageData {

    private final long id;
    private final long channelId;
    @Nullable
    private final UserData author;
    private final String content;
    private final String timestamp;
    @Nullable
    private final String editedTimestamp;
    private final boolean tts;
    private final boolean mentionEveryone;
    private final long[] mentions;
    private final long[] mentionRoles;
    private final AttachmentData[] attachments;
    private final EmbedData[] embeds;
    @Nullable
    private final ReactionData[] reactions;
    private final boolean pinned;
    @Nullable
    private final Long webhookId;
    @Nullable
    private final MessageReferenceData messageReference;
    @Nullable
    private final Integer flags;
    private final int type;

    public MessageData(MessageResponse response) {
        id = response.getId();
        channelId = response.getChannelId();
        author = (response.getWebhookId() == null) ? new UserData(response.getAuthor()) : null;
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
                .map(AttachmentData::new)
                .toArray(AttachmentData[]::new);

        embeds = Arrays.stream(response.getEmbeds())
                .map(EmbedData::new)
                .toArray(EmbedData[]::new);

        final ReactionResponse[] reactions = response.getReactions();
        this.reactions = reactions == null ? null : Arrays.stream(reactions)
                .map(ReactionData::new)
                .toArray(ReactionData[]::new);

        pinned = response.isPinned();
        webhookId = response.getWebhookId();
        messageReference = (response.getMessageReference() != null) ?
                new MessageReferenceData(response.getMessageReference()) : null;
        flags = response.getFlags();
        type = response.getType();
    }

    public long getId() {
        return id;
    }

    public long getChannelId() {
        return channelId;
    }

    @Nullable
    public UserData getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Nullable
    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    public boolean isTts() {
        return tts;
    }

    public boolean isMentionEveryone() {
        return mentionEveryone;
    }

    public long[] getMentions() {
        return mentions;
    }

    public long[] getMentionRoles() {
        return mentionRoles;
    }

    public AttachmentData[] getAttachments() {
        return attachments;
    }

    public EmbedData[] getEmbeds() {
        return embeds;
    }

    @Nullable
    public ReactionData[] getReactions() {
        return reactions;
    }

    public boolean isPinned() {
        return pinned;
    }

    @Nullable
    public Long getWebhookId() {
        return webhookId;
    }

    @Nullable
    public MessageReferenceData getMessageReference() {
        return messageReference;
    }

    @Nullable
    public Integer getFlags() {
        return flags;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MessageData{" +
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
                ", messageReference=" + messageReference +
                ", flags=" + flags +
                ", type=" + type +
                '}';
    }
}
