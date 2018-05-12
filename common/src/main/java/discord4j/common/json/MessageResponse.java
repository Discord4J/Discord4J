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
package discord4j.common.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;
import java.util.Arrays;

public class MessageResponse {

    @UnsignedJson
    private long id;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private UserResponse author;
    @Nullable
    private String content;
    private String timestamp;
    @JsonProperty("edited_timestamp")
    @Nullable
    private String editedTimestamp;
    private boolean tts;
    @JsonProperty("mention_everyone")
    private boolean mentionEveryone;
    private UserResponse[] mentions;
    @JsonProperty("mention_roles")
    @UnsignedJson
    private long[] mentionRoles;
    private AttachmentResponse[] attachments;
    private EmbedResponse[] embeds;
    @Nullable
    private ReactionResponse[] reactions;
    @Nullable
    @UnsignedJson
    private Long nonce;
    private boolean pinned;
    @JsonProperty("webhook_id")
    @Nullable
    @UnsignedJson
    private Long webhookId;
    private int type;

    public long getId() {
        return id;
    }

    public long getChannelId() {
        return channelId;
    }

    public UserResponse getAuthor() {
        return author;
    }

    @Nullable
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

    public UserResponse[] getMentions() {
        return mentions;
    }

    public long[] getMentionRoles() {
        return mentionRoles;
    }

    public AttachmentResponse[] getAttachments() {
        return attachments;
    }

    public EmbedResponse[] getEmbeds() {
        return embeds;
    }

    @Nullable
    public ReactionResponse[] getReactions() {
        return reactions;
    }

    @Nullable
    public Long getNonce() {
        return nonce;
    }

    public boolean isPinned() {
        return pinned;
    }

    @Nullable
    public Long getWebhookId() {
        return webhookId;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MessageResponse[" +
                "id=" + id +
                ", channelId=" + channelId +
                ", author=" + author +
                ", content=" + content +
                ", timestamp=" + timestamp +
                ", editedTimestamp=" + editedTimestamp +
                ", tts=" + tts +
                ", mentionEveryone=" + mentionEveryone +
                ", mentions=" + Arrays.toString(mentions) +
                ", mentionRoles=" + Arrays.toString(mentionRoles) +
                ", attachments=" + Arrays.toString(attachments) +
                ", embeds=" + Arrays.toString(embeds) +
                ", reactions=" + Arrays.toString(reactions) +
                ", nonce=" + nonce +
                ", pinned=" + pinned +
                ", webhookId=" + webhookId +
                ", type=" + type +
                ']';
    }
}
