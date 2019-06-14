package discord4j.common.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class MessageMember {

    @UnsignedJson
    private long[] roles;
    @Nullable
    private String nick;
    private boolean mute;
    private boolean deaf;
    @JsonProperty("joined_at")
    private String joinedAt;
    @JsonProperty("premium_since")
    private String premiumSince;

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

    @Nullable
    public String getPremiumSince() {
        return premiumSince;
    }

    @Override
    public String toString() {
        return "MessageMember{" +
            "roles=" + Arrays.toString(roles) +
            ", nick='" + nick + '\'' +
            ", mute=" + mute +
            ", deaf=" + deaf +
            ", joinedAt='" + joinedAt + '\'' +
            ", premiumSince='" + premiumSince + '\'' +
            '}';
    }

}
