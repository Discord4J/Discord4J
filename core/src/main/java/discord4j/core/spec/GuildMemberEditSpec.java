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
package discord4j.core.spec;

import discord4j.core.object.util.Snowflake;
import discord4j.rest.json.request.GuildMemberModifyRequest;

import javax.annotation.Nullable;
import java.util.Set;

public class GuildMemberEditSpec implements AuditSpec<GuildMemberModifyRequest> {

    private final GuildMemberModifyRequest.Builder builder = GuildMemberModifyRequest.builder();
    @Nullable
    private String reason;

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
        builder.nick(nickname);
        return this;
    }

    public GuildMemberEditSpec setRoles(Set<Snowflake> roles) {
        builder.roles(roles.stream().mapToLong(Snowflake::asLong).toArray());
        return this;
    }

    @Override
    public GuildMemberEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public GuildMemberModifyRequest asRequest() {
        return builder.build();
    }
}
