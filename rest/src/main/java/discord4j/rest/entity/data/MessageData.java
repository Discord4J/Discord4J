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

    private long id;
    private long channelId;
    @Nullable
    private UserData author;
    private String content;
    private String timestamp;
    @Nullable
    private String editedTimestamp;
    private boolean tts;
    private boolean mentionEveryone;
    private long[] mentions;
    private long[] mentionRoles;
    private AttachmentData[] attachments;
    private EmbedData[] embeds;
    @Nullable
    private ReactionData[] reactions;
    private boolean pinned;
    @Nullable
    private Long webhookId;
    private int type;

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
        type = response.getType();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    @Nullable
    public UserData getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable UserData author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    public void setEditedTimestamp(@Nullable String editedTimestamp) {
        this.editedTimestamp = editedTimestamp;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public boolean isMentionEveryone() {
        return mentionEveryone;
    }

    public void setMentionEveryone(boolean mentionEveryone) {
        this.mentionEveryone = mentionEveryone;
    }

    public long[] getMentions() {
        return mentions;
    }

    public void setMentions(long[] mentions) {
        this.mentions = mentions;
    }

    public long[] getMentionRoles() {
        return mentionRoles;
    }

    public void setMentionRoles(long[] mentionRoles) {
        this.mentionRoles = mentionRoles;
    }

    public AttachmentData[] getAttachments() {
        return attachments;
    }

    public void setAttachments(AttachmentData[] attachments) {
        this.attachments = attachments;
    }

    public EmbedData[] getEmbeds() {
        return embeds;
    }

    public void setEmbeds(EmbedData[] embeds) {
        this.embeds = embeds;
    }

    @Nullable
    public ReactionData[] getReactions() {
        return reactions;
    }

    public void setReactions(@Nullable ReactionData[] reactions) {
        this.reactions = reactions;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Nullable
    public Long getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(@Nullable Long webhookId) {
        this.webhookId = webhookId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
                ", type=" + type +
                '}';
    }
}
