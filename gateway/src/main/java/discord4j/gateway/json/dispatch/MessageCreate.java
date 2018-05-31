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

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.AttachmentResponse;
import discord4j.common.json.EmbedResponse;
import discord4j.common.json.UserResponse;

import javax.annotation.Nullable;
import java.util.Arrays;

public class MessageCreate implements Dispatch {

    private int type;
    private boolean tts;
    private String timestamp;
    private boolean pinned;
    private String nonce;
    private Mention[] mentions;
    @JsonProperty("mention_roles")
    @UnsignedJson
    private long[] mentionRoles;
    @JsonProperty("mention_everyone")
    private boolean mentionEveryone;
    @Nullable
    private MessageMember member;
    @UnsignedJson
    private long id;
    private EmbedResponse[] embeds;
    @JsonProperty("edited_timestamp")
    @Nullable
    private String editedTimestamp;
    @Nullable
    private String content;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private UserResponse author;
    private AttachmentResponse[] attachments;
    @JsonProperty("guild_id")
    @Nullable
    @UnsignedJson
    private Long guildId;
    @JsonProperty("webhook_id")
    @Nullable
    @UnsignedJson
    private Long webhookId;
    @Nullable
    private Activity activity;
    @Nullable
    private Application application;

    public int getType() {
        return type;
    }

    public boolean isTts() {
        return tts;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isPinned() {
        return pinned;
    }

    public String getNonce() {
        return nonce;
    }

    public Mention[] getMentions() {
        return mentions;
    }

    public long[] getMentionRoles() {
        return mentionRoles;
    }

    public boolean isMentionEveryone() {
        return mentionEveryone;
    }

    @Nullable
    public MessageMember getMember() {
        return member;
    }

    public long getId() {
        return id;
    }

    public EmbedResponse[] getEmbeds() {
        return embeds;
    }

    @Nullable
    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public long getChannelId() {
        return channelId;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public AttachmentResponse[] getAttachments() {
        return attachments;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    @Nullable
    public Long getWebhookId() {
        return webhookId;
    }

    @Nullable
    public Activity getActivity() {
        return activity;
    }

    @Nullable
    public Application getApplication() {
        return application;
    }

    @Override
    public String toString() {
        return "MessageCreate{" +
                "type=" + type +
                ", tts=" + tts +
                ", timestamp='" + timestamp + '\'' +
                ", pinned=" + pinned +
                ", nonce='" + nonce + '\'' +
                ", mentions=" + Arrays.toString(mentions) +
                ", mentionRoles=" + Arrays.toString(mentionRoles) +
                ", mentionEveryone=" + mentionEveryone +
                ", member=" + member +
                ", id=" + id +
                ", embeds=" + Arrays.toString(embeds) +
                ", editedTimestamp='" + editedTimestamp + '\'' +
                ", content='" + content + '\'' +
                ", channelId=" + channelId +
                ", author=" + author +
                ", attachments=" + Arrays.toString(attachments) +
                ", guildId=" + guildId +
                ", webhookId=" + webhookId +
                ", activity=" + activity +
                ", application=" + application +
                '}';
    }

    public static class Activity {
        private int type;
        @JsonProperty("party_id")
        @Nullable
        private String partyId;

        public int getType() {
            return type;
        }

        @Nullable
        public String getPartyId() {
            return partyId;
        }

        @Override
        public String toString() {
            return "Activity{" +
                    "type=" + type +
                    ", partyId='" + partyId + '\'' +
                    '}';
        }
    }

    public static class Application {

        @UnsignedJson
        private long id;
        @JsonProperty("cover_image")
        private String coverImage;
        private String description;
        private String icon;
        private String name;

        public long getId() {
            return id;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Application{" +
                    "id=" + id +
                    ", coverImage='" + coverImage + '\'' +
                    ", description='" + description + '\'' +
                    ", icon='" + icon + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class Mention {

        private String username;
        @UnsignedJson
        private long id;
        private String discriminator;
        @Nullable
        private String avatar;
        private MessageMember member;
        @Nullable
        private Boolean bot;

        public String getUsername() {
            return username;
        }

        public long getId() {
            return id;
        }

        public String getDiscriminator() {
            return discriminator;
        }

        @Nullable
        public String getAvatar() {
            return avatar;
        }

        public MessageMember getMember() {
            return member;
        }

        @Nullable
        public Boolean getBot() {
            return bot;
        }

        @Override
        public String toString() {
            return "Mention{" +
                    "username='" + username + '\'' +
                    ", id=" + id +
                    ", discriminator='" + discriminator + '\'' +
                    ", avatar='" + avatar + '\'' +
                    ", member=" + member +
                    ", bot=" + bot +
                    '}';
        }
    }

    public static class MessageMember {

        @UnsignedJson
        private long[] roles;
        @Nullable
        private String nick;
        private boolean mute;
        private boolean deaf;
        @JsonProperty("joined_at")
        private String joinedAt;

        public long[] getRoles() {
            return roles;
        }

        @Nullable
        public String getNick() {
            return nick;
        }

        public boolean isMute() {
            return mute;
        }

        public boolean isDeaf() {
            return deaf;
        }

        public String getJoinedAt() {
            return joinedAt;
        }

        @Override
        public String toString() {
            return "MessageMember{" +
                    "roles=" + Arrays.toString(roles) +
                    ", nick='" + nick + '\'' +
                    ", mute=" + mute +
                    ", deaf=" + deaf +
                    ", joinedAt='" + joinedAt + '\'' +
                    '}';
        }
    }
}
