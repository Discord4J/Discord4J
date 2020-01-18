package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.UserResponse;

public class InviteCreate implements Dispatch {

    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private String code;
    @JsonProperty("created_at")
    private String createdAt;
    private int uses;
    @JsonProperty("max_uses")
    private int maxUses;
    @JsonProperty("max_age")
    private int maxAge;
    private Boolean temporary;
    //TODO: Inviter return null
    @JsonUnwrapped
    private UserResponse inviter;

    public UserResponse getInviter() {
        return inviter;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getCode() {
        return code;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getUses() {
        return uses;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public Boolean isTemporary() {
        return temporary;
    }

    @Override
    public String toString() {
        return "InviteCreate{" +
            "code='" + code + '\'' +
            ", guildId=" + guildId +
            ", channelId=" + channelId +
            ", inviter=" + inviter +
            ", uses=" + uses +
            ", maxUses=" + maxUses +
            ", maxAge=" + maxAge +
            ", temporary=" + temporary +
            ", createdAt='" + createdAt +
            '}';
    }
}
