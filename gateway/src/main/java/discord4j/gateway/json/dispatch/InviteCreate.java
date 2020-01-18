package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

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
    private Integer uses;
    @JsonProperty("max_uses")
    @Nullable
    private Integer maxUses;
    @JsonProperty("max_age")
    @Nullable
    private Integer maxAge;
    @Nullable
    private Boolean temporary;
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

    @Nullable
    public Integer getUses() {
        return uses;
    }

    @Nullable
    public Integer getMaxUses() {
        return maxUses;
    }

    @Nullable
    public Integer getMaxAge() {
        return maxAge;
    }

    @Nullable
    public Boolean getTemporary() {
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
