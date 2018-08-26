package discord4j.core.spec;

import discord4j.core.object.util.Snowflake;
import discord4j.rest.json.request.GuildMemberModifyRequest;

import javax.annotation.Nullable;
import java.util.Set;

public class GuildMemberEditSpec implements Spec<GuildMemberModifyRequest> {

    private final GuildMemberModifyRequest.Builder builder = GuildMemberModifyRequest.builder();

    public GuildMemberEditSpec setNewVoiceChannel(@Nullable Snowflake channel) {
        builder.channelId(channel == null ? null : channel.asLong());
        return this;
    }

    public GuildMemberEditSpec setMute(boolean mute) {
        builder.mute(mute);
        return this;
    }

    public GuildMemberEditSpec setDeafen(boolean deaf) {
        builder.deaf(deaf);
        return this;
    }

    public GuildMemberEditSpec setNickname(@Nullable String nickname) {
        builder.nick(nickname == null ? "" : nickname);
        return this;
    }

    public GuildMemberEditSpec setRoles(Set<Snowflake> roles) {
        builder.roles(roles.stream().mapToLong(Snowflake::asLong).toArray());
        return this;
    }

    @Override
    public GuildMemberModifyRequest asRequest() {
        return builder.build();
    }
}
