package discord4j.common.json;

import discord4j.common.jackson.UnsignedJson;
import reactor.util.annotation.Nullable;

public class Mention {

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
