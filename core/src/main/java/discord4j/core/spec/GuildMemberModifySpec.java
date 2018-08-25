package discord4j.core.spec;

import discord4j.core.object.util.Snowflake;
import discord4j.rest.json.request.GuildMemberModifyRequest;

import javax.annotation.Nullable;
import java.util.Set;

public class GuildMemberModifySpec implements Spec<GuildMemberModifyRequest> {

    private final GuildMemberModifyRequest.Builder builder = GuildMemberModifyRequest.builder();

    public GuildMemberModifySpec setNewVoiceChannel(@Nullable Snowflake channel) {
        builder.channelId(channel == null ? null : channel.asLong());
        return this;
    }

    public GuildMemberModifySpec setMute(boolean mute) {
        builder.mute(mute);
        return this;
    }

    public GuildMemberModifySpec setDeafen(boolean deaf) {
        builder.deaf(deaf);
        return this;
    }

    public GuildMemberModifySpec setNickname(@Nullable String nickname) {
        builder.nick(nickname == null ? "" : nickname);
        return this;
    }

    public GuildMemberModifySpec setRoles(@Nullable Set<Snowflake> roles) {
        builder.roles(roles == null ? null : roles.stream().mapToLong(Snowflake::asLong).toArray());
        return this;
    }


    @Override
    public GuildMemberModifyRequest asRequest() {
        return builder.build();
    }
}
