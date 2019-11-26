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
package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.AttachmentResponse;
import discord4j.common.json.EmbedResponse;
import discord4j.common.json.Mention;
import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageUpdate implements Dispatch {

    private int type;
    private boolean tts;
    private Possible<String> timestamp = Possible.absent();
    private Possible<Boolean> pinned = Possible.absent();
    @Nullable
    private String nonce;
    private Possible<Mention[]> mentions = Possible.absent();
    @JsonProperty("mention_roles")
    private Possible<long[]> mentionRoles = Possible.absent();
    @JsonProperty("mention_everyone")
    private Possible<Boolean> mentionEveryone = Possible.absent();
    @UnsignedJson
    private long id;
    private Possible<EmbedResponse[]> embeds = Possible.absent();
    @JsonProperty("edited_timestamp")
    @Nullable
    private String editedTimestamp;
    private Possible<String> content = Possible.absent();
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private Possible<UserResponse> author = Possible.absent();
    private Possible<AttachmentResponse[]> attachments = Possible.absent();
    @JsonProperty("guild_id")
    @UnsignedJson
    @Nullable
    private Long guildId;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public int getType() {
        return type;
    }

    public boolean isTts() {
        return tts;
    }

    public Possible<String> getTimestamp() {
        return timestamp;
    }

    public Possible<Boolean> isPinned() {
        return pinned;
    }

    @Nullable
    public String getNonce() {
        return nonce;
    }

    public Possible<Mention[]> getMentions() {
        return mentions;
    }

    public Possible<long[]> getMentionRoles() {
        return mentionRoles;
    }

    public Possible<Boolean> getMentionEveryone() {
        return mentionEveryone;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Possible<EmbedResponse[]> getEmbeds() {
        return embeds;
    }

    @Nullable
    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    public Possible<String> getContent() {
        return content;
    }

    public long getChannelId() {
        return channelId;
    }

    public Possible<UserResponse> getAuthor() {
        return author;
    }

    public Possible<AttachmentResponse[]> getAttachments() {
        return attachments;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "MessageUpdate{" +
                "type=" + type +
                ", tts=" + tts +
                ", timestamp='" + timestamp + "'" +
                ", pinned=" + pinned +
                ", nonce='" + nonce + "'" +
                ", mentions=" + (mentions.isAbsent() ? mentions : Arrays.toString(mentions.get())) +
                ", mentionRoles=" + (mentionRoles.isAbsent() ? mentionRoles : Arrays.toString(mentionRoles.get())) +
                ", mentionEveryone=" + mentionEveryone +
                ", id=" + id +
                ", embeds=" + embeds +
                ", editedTimestamp=" + editedTimestamp +
                ", content=" + content +
                ", channelId=" + channelId +
                ", author=" + author +
                ", attachments=" + (attachments.isAbsent() ? attachments : Arrays.toString(attachments.get())) +
                ", guildId=" + guildId +
                '}';
    }

}
